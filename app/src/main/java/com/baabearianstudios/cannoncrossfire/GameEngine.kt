package com.baabearianstudios.cannoncrossfire

import android.content.Context
import kotlin.math.*
import kotlin.random.Random

class GameEngine(
    private val context: Context,
    private val soundManager: GameSoundManager,
    private val onStateChanged: (GameState) -> Unit,
    private val onScoreChanged: (Int) -> Unit
) {
    var width: Float = 1080f
    var height: Float = 1920f

    var gameState: GameState = GameState.Ready
        private set(value) {
            field = value
            onStateChanged(value)
        }

    var score: Int = 0
        private set(value) {
            field = value
            onScoreChanged(value)
        }

    val player: Player = Player(width / 2f, height / 2f, 32f)
    val cannons: MutableList<Cannon> = mutableListOf()
    val cannonballs: MutableList<Cannonball> = mutableListOf()
    var coin: Coin? = null

    var tiltAccelX: Float = 0f
    var tiltAccelY: Float = 0f

    private var gameTime: Long = 0
    private var lastScoreUpdateTime: Long = 0
    private var nextCannonId: Int = 0
    private var nextCannonSpawnTime: Long = 0
    private var isCalibrated: Boolean = false
    private var calibOffsetX: Float = 0f
    private var calibOffsetY: Float = 0f

    fun updateDimensions(w: Float, h: Float) {
        if (w <= 0f || h <= 0f) return
        width = w
        height = h
        if (gameState is GameState.Ready) {
            player.x = width / 2f
            player.y = height / 2f
            player.radius = (width * 0.08f).coerceIn(26f, 34f)
        }
    }

    fun startGame() {
        soundManager.playSelect()
        gameTime = 0
        score = 0
        lastScoreUpdateTime = 0

        player.x = width / 2f
        player.y = height / 2f
        player.radius = (width * 0.08f).coerceIn(26f, 34f)
        player.velX = 0f
        player.velY = 0f

        cannons.clear()
        cannonballs.clear()
        nextCannonId = 0

        val initialCount = if (width < 500f) 5 else 6
        for (i in 0 until initialCount) {
            cannons.add(spawnSingleCannon())
        }

        spawnCoin()
        nextCannonSpawnTime = 6000

        gameState = GameState.Countdown(3)
        soundManager.playGameMusic()
    }

    fun stepCountdown(nextCount: Int) {
        if (nextCount >= 1) {
            gameState = GameState.Countdown(nextCount)
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

    fun calibrateTilt() {
        calibOffsetX = tiltAccelX
        calibOffsetY = tiltAccelY
        isCalibrated = true
        soundManager.playSelect()
    }

    fun updateTilt(accelX: Float, accelY: Float) {
        val normX = (-accelX.coerceIn(-9.8f, 9.8f) / 9.8f)
        val normY = ((accelY - 4.5f).coerceIn(-9.8f, 9.8f) / 9.8f)
        tiltAccelX = normX * 3.2f - (if (isCalibrated) calibOffsetX else 0f)
        tiltAccelY = normY * 3.2f - (if (isCalibrated) calibOffsetY else 0f)
    }

    fun update(dtMs: Long) {
        if (gameState !is GameState.Running) return
        val dt = dtMs.coerceAtMost(50)
        gameTime += dt

        // 1. Time-based score increments
        if (gameTime - lastScoreUpdateTime >= 100) {
            val points = ((gameTime - lastScoreUpdateTime) / 100).toInt()
            score += points
            lastScoreUpdateTime = gameTime
        }

        // 2. Player tilt physics
        val sensitivity = 0.32f
        player.velX += -tiltAccelX * sensitivity
        player.velY += tiltAccelY * sensitivity

        player.velX *= 0.89f
        player.velY *= 0.89f

        val maxSpeed = 7.0f
        player.velX = player.velX.coerceIn(-maxSpeed, maxSpeed)
        player.velY = player.velY.coerceIn(-maxSpeed, maxSpeed)

        player.x += player.velX
        player.y += player.velY

        // Clamp inside screen
        player.x = player.x.coerceIn(0f, width)
        player.y = player.y.coerceIn(0f, height)

        // 3. Update Cannonballs
        val startSpeed = 1.8f
        val maxSpeedCap = 3.8f
        val currentSpeed = min(maxSpeedCap, startSpeed + (gameTime.toFloat() / 40000f) * (maxSpeedCap - startSpeed))

        val iterator = cannonballs.iterator()
        while (iterator.hasNext()) {
            val cb = iterator.next()
            cb.x += cb.velocityX
            cb.y += cb.velocityY

            if (cb.x < 0 || cb.x > width || cb.y < 0 || cb.y > height) {
                iterator.remove()
                val cannonIdx = cannons.indexOfFirst { it.id == cb.cannonId }
                if (cannonIdx != -1) {
                    cannons.removeAt(cannonIdx)
                    cannons.add(spawnSingleCannon())
                }
            }
        }

        // 4. Fire cannons
        for (cannon in cannons) {
            if (gameTime >= cannon.nextFireTime) {
                val rad = Math.toRadians(cannon.angle.toDouble())
                cannonballs.add(
                    Cannonball(
                        id = Random.nextInt(1_000_000),
                        cannonId = cannon.id,
                        x = cannon.x,
                        y = cannon.y,
                        velocityX = (cos(rad) * currentSpeed).toFloat(),
                        velocityY = (sin(rad) * currentSpeed).toFloat(),
                        radius = (width * 0.035f).coerceIn(12f, 15f)
                    )
                )
                cannon.nextFireTime = Long.MAX_VALUE
                soundManager.playShoot()
            }
        }

        // 5. Spawn new cannons over time
        if (gameTime >= nextCannonSpawnTime) {
            val maxCannons = if (width < 500f) 8 else 11
            if (cannons.size < maxCannons) {
                cannons.add(spawnSingleCannon())
            }
            val delay = max(2000L, 6000L - (gameTime / 15))
            nextCannonSpawnTime = gameTime + delay
        }

        // 6. Collision: Player vs Cannonballs
        for (cb in cannonballs) {
            val dx = player.x - cb.x
            val dy = player.y - cb.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < (player.radius + cb.radius) * 0.85f) {
                soundManager.playHit()
                triggerGameOver()
                return
            }
        }

        // 7. Coin Collection
        coin?.let { c ->
            val dx = player.x - c.x
            val dy = player.y - c.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < player.radius + c.radius) {
                score += 50
                soundManager.playCoin()
                spawnCoin()
            }
        }

        // 8. Kill Zone Border Check
        val killZoneLimit = 16f + player.radius * 0.45f
        if (player.x < killZoneLimit || player.x > width - killZoneLimit ||
            player.y < killZoneLimit || player.y > height - killZoneLimit) {
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
        val centerX = width / 2f
        val centerY = height / 2f
        val cannonRadius = (width * 0.08f).coerceIn(26f, 36f)

        val side = Random.nextInt(4)
        val x: Float
        val y: Float

        when (side) {
            0 -> { // Top
                x = Random.nextFloat() * (width - cannonRadius * 2f) + cannonRadius
                y = cannonRadius
            }
            1 -> { // Right
                x = width - cannonRadius
                y = Random.nextFloat() * (height - cannonRadius * 2f) + cannonRadius
            }
            2 -> { // Bottom
                x = Random.nextFloat() * (width - cannonRadius * 2f) + cannonRadius
                y = height - cannonRadius
            }
            else -> { // Left
                x = cannonRadius
                y = Random.nextFloat() * (height - cannonRadius * 2f) + cannonRadius
            }
        }

        val angleToCenter = Math.toDegrees(atan2((centerY - y).toDouble(), (centerX - x).toDouble())).toFloat()

        return Cannon(
            id = nextCannonId++,
            x = x,
            y = y,
            angle = angleToCenter,
            radius = cannonRadius,
            nextFireTime = gameTime + Random.nextLong(400, 1200)
        )
    }

    private fun spawnCoin() {
        val padding = (width * 0.12f).coerceIn(45f, 65f)
        val x = Random.nextFloat() * (width - padding * 2f) + padding
        val y = Random.nextFloat() * (height - padding * 2f) + padding
        val coinRadius = (width * 0.065f).coerceIn(22f, 28f)
        coin = Coin(x, y, coinRadius)
    }
}
