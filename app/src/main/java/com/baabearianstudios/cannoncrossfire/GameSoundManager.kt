package com.baabearianstudios.cannoncrossfire

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class GameSoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var sfxCoinId: Int = 0
    private var sfxHitId: Int = 0
    private var sfxSelectId: Int = 0
    private var sfxShootId: Int = 0

    private var bgmMenuPlayer: MediaPlayer? = null
    private var bgmGamePlayer: MediaPlayer? = null
    private var currentTrack: String? = null
    private var isMuted: Boolean = false
    private val volume: Float = 0.7f

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.let { sp ->
            sfxCoinId = sp.load(context, R.raw.sfx_coin, 1)
            sfxHitId = sp.load(context, R.raw.sfx_hit, 1)
            sfxSelectId = sp.load(context, R.raw.sfx_select, 1)
            sfxShootId = sp.load(context, R.raw.sfx_shoot, 1)
        }
    }

    fun playCoin() = playSound(sfxCoinId)
    fun playHit() = playSound(sfxHitId)
    fun playSelect() = playSound(sfxSelectId)
    fun playShoot() = playSound(sfxShootId)

    private fun playSound(soundId: Int) {
        if (isMuted || soundId == 0) return
        soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    fun playMenuMusic() {
        if (currentTrack == "menu" && bgmMenuPlayer?.isPlaying == true) return
        stopGameMusic()
        currentTrack = "menu"

        if (isMuted) return
        try {
            if (bgmMenuPlayer == null) {
                bgmMenuPlayer = MediaPlayer.create(context, R.raw.bgm_menu)?.apply {
                    isLooping = true
                    setVolume(volume * 0.8f, volume * 0.8f)
                }
            }
            bgmMenuPlayer?.seekTo(0)
            bgmMenuPlayer?.start()
        } catch (_: Exception) {
        }
    }

    fun playGameMusic() {
        if (currentTrack == "game" && bgmGamePlayer?.isPlaying == true) return
        stopMenuMusic()
        currentTrack = "game"

        if (isMuted) return
        try {
            if (bgmGamePlayer == null) {
                bgmGamePlayer = MediaPlayer.create(context, R.raw.bgm_game)?.apply {
                    isLooping = true
                    setVolume(volume * 0.8f, volume * 0.8f)
                }
            }
            bgmGamePlayer?.seekTo(0)
            bgmGamePlayer?.start()
        } catch (_: Exception) {
        }
    }

    fun stopMenuMusic() {
        try {
            if (bgmMenuPlayer?.isPlaying == true) {
                bgmMenuPlayer?.pause()
            }
        } catch (_: Exception) {
        }
    }

    fun stopGameMusic() {
        try {
            if (bgmGamePlayer?.isPlaying == true) {
                bgmGamePlayer?.pause()
            }
        } catch (_: Exception) {
        }
    }

    fun onAppPaused() {
        stopMenuMusic()
        stopGameMusic()
    }

    fun onAppResumed() {
        if (isMuted) return
        when (currentTrack) {
            "menu" -> playMenuMusic()
            "game" -> playGameMusic()
        }
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            stopMenuMusic()
            stopGameMusic()
        } else {
            onAppResumed()
        }
        return isMuted
    }

    fun isMuted(): Boolean = isMuted

    fun release() {
        try {
            bgmMenuPlayer?.stop()
            bgmMenuPlayer?.release()
            bgmMenuPlayer = null

            bgmGamePlayer?.stop()
            bgmGamePlayer?.release()
            bgmGamePlayer = null

            soundPool?.release()
            soundPool = null
        } catch (_: Exception) {
        }
    }
}
