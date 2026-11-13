package com.baabearianstudios.cannoncrossfire

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val soundManager = GameSoundManager(appContext)
    private val engine = GameEngine(appContext, soundManager)

    private var cachedHighScore = ScoreStorage.getLocalHighScore(appContext)
    private var cachedLeaderboard = ScoreStorage.getLeaderboard(appContext)

    private val _uiState = MutableStateFlow(
        engine.snapshot(
            highScore = cachedHighScore,
            leaderboard = cachedLeaderboard,
            isMuted = soundManager.isMuted()
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var flatStableSinceMs: Long = 0L

    init {
        soundManager.playMenuMusic()
    }

    fun updateArenaSize(dpWidth: Float, dpHeight: Float) {
        engine.updateWorldDimensions(dpWidth, dpHeight)
        publishSnapshot()
    }

    fun startGame() {
        countdownJob?.cancel()
        flatStableSinceMs = 0L
        val startedImmediately = engine.requestStartGame()
        publishSnapshot()
        if (startedImmediately) {
            startCountdown()
        }
    }

    fun pauseGame() {
        countdownJob?.cancel()
        engine.pauseGame()
        publishSnapshot()
    }

    fun resumeGame() {
        countdownJob?.cancel()
        engine.resumeGame()
        publishSnapshot()
        startCountdown()
    }

    fun quitToMenu() {
        countdownJob?.cancel()
        flatStableSinceMs = 0L
        engine.quitToMenu()
        cachedHighScore = ScoreStorage.getLocalHighScore(appContext)
        cachedLeaderboard = ScoreStorage.getLeaderboard(appContext)
        publishSnapshot()
    }

    fun submitScore(playerName: String) {
        soundManager.playSelect()
        val finalScore = (engine.gameState as? GameState.GameOver)?.score ?: engine.score
        cachedLeaderboard = ScoreStorage.submitScore(appContext, playerName, finalScore)
        cachedHighScore = ScoreStorage.getLocalHighScore(appContext)
        publishSnapshot()
    }

    fun toggleMute() {
        soundManager.toggleMute()
        publishSnapshot()
    }

    fun onAccelerometerChanged(accelX: Float, accelY: Float, accelZ: Float) {
        engine.updateSensorTilt(accelX, accelY, accelZ)
        if (engine.gameState is GameState.WaitingForFlat) {
            val now = System.currentTimeMillis()
            if (engine.isPhoneFlat()) {
                if (flatStableSinceMs == 0L) {
                    flatStableSinceMs = now
                } else if (now - flatStableSinceMs >= 150L) {
                    flatStableSinceMs = 0L
                    soundManager.playSelect()
                    engine.beginCountdown()
                    publishSnapshot()
                    startCountdown()
                    return
                }
            } else {
                flatStableSinceMs = 0L
            }
            publishSnapshot()
        }
    }

    fun stepFrame(dtMs: Long) {
        val prevState = engine.gameState
        engine.update(dtMs)
        if (prevState != engine.gameState && engine.gameState is GameState.GameOver) {
            cachedHighScore = ScoreStorage.getLocalHighScore(appContext)
            cachedLeaderboard = ScoreStorage.getLeaderboard(appContext)
        }
        publishSnapshot()
    }

    fun onAppPaused() {
        if (engine.gameState is GameState.Running) {
            engine.pauseGame()
            publishSnapshot()
        }
        soundManager.onAppPaused()
    }

    fun onAppResumed() {
        soundManager.onAppResumed()
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (next in listOf(2, 1, 0)) {
                delay(1000L)
                engine.setCountdown(next)
                publishSnapshot()
            }
        }
    }

    private fun publishSnapshot() {
        _uiState.value = engine.snapshot(
            highScore = cachedHighScore,
            leaderboard = cachedLeaderboard,
            isMuted = soundManager.isMuted()
        )
    }

    override fun onCleared() {
        countdownJob?.cancel()
        soundManager.release()
        super.onCleared()
    }
}
