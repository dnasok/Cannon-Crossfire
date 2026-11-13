package com.baabearianstudios.cannoncrossfire

sealed class GameState {
    data object Ready : GameState()
    data object WaitingForFlat : GameState()
    data class Countdown(val count: Int) : GameState()
    data object Running : GameState()
    data object Paused : GameState()
    data class GameOver(val score: Int, val isNewHighScore: Boolean) : GameState()
}

data class Player(
    val x: Float,
    val y: Float,
    val radius: Float,
    val velX: Float = 0f,
    val velY: Float = 0f
)

data class Cannon(
    val id: Int,
    val x: Float,
    val y: Float,
    val angle: Float,
    val radius: Float,
    val nextFireTime: Long
)

data class Cannonball(
    val id: Int,
    val cannonId: Int,
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val radius: Float
)

data class Coin(
    val x: Float,
    val y: Float,
    val radius: Float
)

data class OnlineScore(
    val playerName: String,
    val score: Int
)

enum class Side {
    LEFT, RIGHT, TOP, BOTTOM
}

data class MenuBall(
    val side: Side,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val r: Float
)

data class GameUiState(
    val gameState: GameState = GameState.Ready,
    val score: Int = 0,
    val highScore: Int = 0,
    val leaderboard: List<OnlineScore> = emptyList(),
    val isMuted: Boolean = false,
    val hasTiltSensor: Boolean = false,
    val tiltNormX: Float = 0f,
    val tiltNormY: Float = 0f,
    val isPhoneFlat: Boolean = false,
    val player: Player = Player(200f, 350f, 22.5f),
    val cannons: List<Cannon> = emptyList(),
    val cannonballs: List<Cannonball> = emptyList(),
    val coin: Coin? = null,
    val gameTimeMs: Long = 0L
)
