package com.baabearianstudios.cannoncrossfire

import android.content.Context
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Core arcade survival game engine operating in density-independent logical coordinates (dp)
 * with frame-rate normalized 60Hz-equivalent physics and flat-table tilt-only steering.
 */
class GameEngine(
    private val context: Context,
    private val soundManager: GameSoundManager
) {
    companion object {
        private const val ENTITY_SCALE = 0.75f
        private const val FLAT_ACCEL_TOLERANCE = 1.8f
        private const val FLAT_Z_MIN = 7.5f
        private const val TILT_FULL_RANGE = 6.5f
    }

    var worldWidth: Float = 400f
        private set
    var worldHeight: Float = 750f
        private set

    var gameState: GameState = GameState.Ready
        private set
    var score: Int = 0
        private set

    private var playerX: Float = 200f
    private var playerY: Float = 375f
    private var playerRadius: Float = 22.5f
    private var velX: Float = 0f
    private var velY: Float = 0f

    private val cannons: MutableList<Cannon> = mutableListOf()
    private val cannonballs: MutableList<Cannonball> = mutableListOf()
    private var coin: Coin? = null

    // Hardware accelerometer readings (m/s^2); default flat on table is (0, 0, +9.81)
    private var rawAccelX: Float = 0f
    private var rawAccelY: Float = 0f
    private var rawAccelZ: Float = 9.81f
    private var hasTiltSensor: Boolean = false

    // Normalized steering input (-1..1) from tilt only
    private var tiltNormX: Float = 0f
    private var tiltNormY: Float = 0f

    private var gameTimeMs: Long = 0L
    private var lastScoreUpdateTimeMs: Long = 0L
    private var nextCannonId: Int = 0
    private var nextCannonballId: Int = 0
    private var nextCannonSpawnTimeMs: Long = 0L

    fun updateWorldDimensions(dpWidth: Float, dpHeight: Float) {
        if (dpWidth <= 0f || dpHeight <= 0f) return
        worldWidth = dpWidth
        worldHeight = dpHeight
        if (gameState is GameState.Ready || gameState is GameState.WaitingForFlat) {
            playerRadius = computePlayerRadius()
            playerX = worldWidth / 2f
            playerY = worldHeight / 2f
        }
    }

    private fun computePlayerRadius(): Float {
        return (worldWidth * 0.08f).coerceIn(26f, 34f) * ENTITY_SCALE
    }

    fun isPhoneFlat(): Boolean {
        if (!hasTiltSensor) return true
        return abs(rawAccelX) <= FLAT_ACCEL_TOLERANCE &&
            abs(rawAccelY) <= FLAT_ACCEL_TOLERANCE &&
            rawAccelZ >= FLAT_Z_MIN
    }

    fun requestStartGame(): Boolean {
        soundManager.playSelect()
        prepareArena()
        return if (isPhoneFlat()) {
            beginCountdown()
            true
        } else {
            gameState = GameState.WaitingForFlat
            false
        }
    }

    fun beginCountdown() {
        gameState = GameState.Countdown(3)
        soundManager.playGameMusic()
    }

    private fun prepareArena() {
        gameTimeMs = 0L
        score = 0
        lastScoreUpdateTimeMs = 0L

        playerRadius = computePlayerRadius()
        playerX = worldWidth / 2f
        playerY = worldHeight / 2f
        velX = 0f
        velY = 0f

        cannons.clear()
        cannonballs.clear()
        nextCannonId = 0
        nextCannonballId = 0

        val initialCount = if (worldWidth < 500f) 5 else 6
        repeat(initialCount) {
            cannons.add(spawnSingleCannon())
        }

        spawnCoin()
        nextCannonSpawnTimeMs = 6000L
    }

    fun setCountdown(count: Int) {
        if (count >= 1) {
            gameState = GameState.Countdown(count)
        } else {
            gameState = GameState.Running
        }
    }

    fun pauseGame() {
        if (gameState is GameState.Running) {
            soundManager.playSelect()
            gameState = GameState.Paused
            soundManager.playMenuMusic()
        }
    }

    fun resumeGame() {
        if (gameState is GameState.Paused) {
            soundManager.playSelect()
            gameState = GameState.Countdown(3)
            soundManager.playGameMusic()
        }
    }

    fun quitToMenu() {
        soundManager.playSelect()
        gameState = GameState.Ready
        soundManager.playMenuMusic()
    }

    fun updateSensorTilt(accelX: Float, accelY: Float, accelZ: Float) {
        hasTiltSensor = true
        rawAccelX = accelX
        rawAccelY = accelY
        rawAccelZ = accelZ

        // Default placement is flat on a table surface: (0, 0, +9.81).
        // Tilting left yields +accelX -> negative velX (left).
        // Tilting top edge down yields -accelY -> negative velY (up).
        tiltNormX = (-accelX / TILT_FULL_RANGE).coerceIn(-1f, 1f)
        tiltNormY = (accelY / TILT_FULL_RANGE).coerceIn(-1f, 1f)
    }

    fun update(dtMs: Long) {
        if (gameState !is GameState.Running) return
        val dt = dtMs.coerceIn(1L, 50L)
        val dtScale = dt / 16.6667f
        gameTimeMs += dt

        // 1. Time-based survival score (+1 every 100ms)
        if (gameTimeMs - lastScoreUpdateTimeMs >= 100L) {
            val points = ((gameTimeMs - lastScoreUpdateTimeMs) / 100L).toInt()
            score += points
            lastScoreUpdateTimeMs += points * 100L
        }

        // 2. Player physics (strictly tilt-only steering)
        val accelInputX = tiltNormX * 3.2f
        val accelInputY = tiltNormY * 3.2f

        val sensitivity = 0.32f
        velX += accelInputX * sensitivity * dtScale
        velY += accelInputY * sensitivity * dtScale

        val damping = 0.89f.pow(dtScale)
        velX *= damping
        velY *= damping

        val maxSpeed = 7.0f
        velX = velX.coerceIn(-maxSpeed, maxSpeed)
        velY = velY.coerceIn(-maxSpeed, maxSpeed)

        playerX = (playerX + velX * dtScale).coerceIn(0f, worldWidth)
        playerY = (playerY + velY * dtScale).coerceIn(0f, worldHeight)

        // 3. Update Cannonballs
        val startSpeed = 1.8f
        val maxSpeedCap = 3.8f
        val currentSpeed = min(
            maxSpeedCap,
            startSpeed + (gameTimeMs.toFloat() / 40000f) * (maxSpeedCap - startSpeed)
        )

        val updatedBalls = ArrayList<Cannonball>(cannonballs.size)
        for (cb in cannonballs) {
            val nx = cb.x + cb.velocityX * dtScale
            val ny = cb.y + cb.velocityY * dtScale
            if (nx < 0f || nx > worldWidth || ny < 0f || ny > worldHeight) {
                val cannonIdx = cannons.indexOfFirst { it.id == cb.cannonId }
                if (cannonIdx != -1) {
                    cannons[cannonIdx] = spawnSingleCannon()
                }
            } else {
                updatedBalls.add(cb.copy(x = nx, y = ny))
            }
        }
        cannonballs.clear()
        cannonballs.addAll(updatedBalls)

        // 4. Fire cannons
        val ballRadius = (worldWidth * 0.035f).coerceIn(12f, 15f) * ENTITY_SCALE
        for (i in cannons.indices) {
            val cannon = cannons[i]
            if (gameTimeMs >= cannon.nextFireTime) {
                val rad = Math.toRadians(cannon.angle.toDouble())
                cannonballs.add(
                    Cannonball(
                        id = nextCannonballId++,
                        cannonId = cannon.id,
                        x = cannon.x,
                        y = cannon.y,
                        velocityX = (cos(rad) * currentSpeed).toFloat(),
                        velocityY = (sin(rad) * currentSpeed).toFloat(),
                        radius = ballRadius
                    )
                )
                cannons[i] = cannon.copy(nextFireTime = Long.MAX_VALUE)
                soundManager.playShoot()
            }
        }

        // 5. Spawn new cannons over time
        if (gameTimeMs >= nextCannonSpawnTimeMs) {
            val maxCannons = if (worldWidth < 500f) 8 else 11
            if (cannons.size < maxCannons) {
                cannons.add(spawnSingleCannon())
            }
            val delay = max(2000L, 6000L - (gameTimeMs / 15L))
            nextCannonSpawnTimeMs = gameTimeMs + delay
        }

        // 6. Collision: Player vs Cannonballs
        for (cb in cannonballs) {
            val dx = playerX - cb.x
            val dy = playerY - cb.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < (playerRadius + cb.radius) * 0.85f) {
                soundManager.playHit()
                triggerGameOver()
                return
            }
        }

        // 7. Coin Collection (+50 pts)
        coin?.let { c ->
            val dx = playerX - c.x
            val dy = playerY - c.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < playerRadius + c.radius) {
                score += 50
                soundManager.playCoin()
                spawnCoin()
            }
        }

        // 8. Kill Zone Border Check
        val killZoneLimit = 16f + playerRadius * 0.45f
        if (playerX < killZoneLimit || playerX > worldWidth - killZoneLimit ||
            playerY < killZoneLimit || playerY > worldHeight - killZoneLimit
        ) {
            soundManager.playHit()
            triggerGameOver()
        }
    }

    private fun triggerGameOver() {
        val finalScore = score
        val isNewHigh = ScoreStorage.saveLocalHighScore(context, finalScore)
        gameState = GameState.GameOver(finalScore, isNewHigh)
        soundManager.playMenuMusic()
    }

    private fun spawnSingleCannon(): Cannon {
        val centerX = worldWidth / 2f
        val centerY = worldHeight / 2f
        val cannonRadius = (worldWidth * 0.08f).coerceIn(26f, 36f) * ENTITY_SCALE
        val safeW = (worldWidth - cannonRadius * 2f).coerceAtLeast(10f)
        val safeH = (worldHeight - cannonRadius * 2f).coerceAtLeast(10f)

        val side = Random.nextInt(4)
        val x: Float
        val y: Float

        when (side) {
            0 -> { // Top
                x = Random.nextFloat() * safeW + cannonRadius
                y = cannonRadius
            }
            1 -> { // Right
                x = worldWidth - cannonRadius
                y = Random.nextFloat() * safeH + cannonRadius
            }
            2 -> { // Bottom
                x = Random.nextFloat() * safeW + cannonRadius
                y = worldHeight - cannonRadius
            }
            else -> { // Left
                x = cannonRadius
                y = Random.nextFloat() * safeH + cannonRadius
            }
        }

        val angleToCenter = Math.toDegrees(
            atan2((centerY - y).toDouble(), (centerX - x).toDouble())
        ).toFloat()

        return Cannon(
            id = nextCannonId++,
            x = x,
            y = y,
            angle = angleToCenter,
            radius = cannonRadius,
            nextFireTime = gameTimeMs + Random.nextLong(400L, 1200L)
        )
    }

    private fun spawnCoin() {
        val padding = (worldWidth * 0.12f).coerceIn(45f, 65f)
        val safeW = (worldWidth - padding * 2f).coerceAtLeast(20f)
        val safeH = (worldHeight - padding * 2f).coerceAtLeast(20f)
        val x = Random.nextFloat() * safeW + padding
        val y = Random.nextFloat() * safeH + padding
        val coinRadius = (worldWidth * 0.065f).coerceIn(22f, 28f) * ENTITY_SCALE
        coin = Coin(x, y, coinRadius)
    }

    fun snapshot(
        highScore: Int,
        leaderboard: List<OnlineScore>,
        isMuted: Boolean
    ): GameUiState {
        return GameUiState(
            gameState = gameState,
            score = score,
            highScore = highScore,
            leaderboard = leaderboard,
            isMuted = isMuted,
            hasTiltSensor = hasTiltSensor,
            tiltNormX = tiltNormX,
            tiltNormY = tiltNormY,
            isPhoneFlat = isPhoneFlat(),
            player = Player(playerX, playerY, playerRadius, velX, velY),
            cannons = cannons.toList(),
            cannonballs = cannonballs.toList(),
            coin = coin,
            gameTimeMs = gameTimeMs
        )
    }
}
