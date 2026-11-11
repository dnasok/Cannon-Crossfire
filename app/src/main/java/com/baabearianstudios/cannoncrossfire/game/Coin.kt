package com.baabearianstudios.cannoncrossfire.game

/**
 * Represents a coin in the game that the player can collect for points.
 *
 * @property x The x-coordinate of the coin's center.
 * @property y The y-coordinate of the coin's center.
 * @property radius The radius of the coin.
 */
data class Coin(val x: Float, val y: Float, val radius: Float = 30f)
