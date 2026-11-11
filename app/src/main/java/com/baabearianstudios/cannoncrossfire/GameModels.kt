package com.baabearianstudios.cannoncrossfire

sealed class GameState {
    object Ready : GameState()
    data class Countdown(val count: Int) : GameState()
    object Running : GameState()
    object Paused : GameState()
    data class GameOver(val score: Int, val isNewHighScore: Boolean) : GameState()
}

data class Player(
    var x: Float,
    var y: Float,
    var radius: Float,
    var velX: Float = 0f,
    var velY: Float = 0f
)

data class Cannon(
    val id: Int,
    val x: Float,
    val y: Float,
    val angle: Float,
    val radius: Float,
    var nextFireTime: Long
)

data class Cannonball(
    val id: Int,
    val cannonId: Int,
    var x: Float,
    var y: Float,
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

data class MenuBall(
    val side: Side,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val r: Float
)

enum class Side {
    LEFT, RIGHT, TOP, BOTTOM
}
