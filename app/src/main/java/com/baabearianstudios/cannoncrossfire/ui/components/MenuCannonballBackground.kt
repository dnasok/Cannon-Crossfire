package com.baabearianstudios.cannoncrossfire.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.baabearianstudios.cannoncrossfire.MenuBall
import com.baabearianstudios.cannoncrossfire.R
import com.baabearianstudios.cannoncrossfire.Side
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun MenuCannonballBackground(modifier: Modifier = Modifier) {
    val cannonballBitmap = ImageBitmap.imageResource(id = R.drawable.cannonball)
    var balls by remember { mutableStateOf<List<MenuBall>>(emptyList()) }
    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }

    fun spawnBall(side: Side, w: Float, h: Float): MenuBall {
        val r = 36f
        val speed = 14f
        return when (side) {
            Side.LEFT -> MenuBall(side, -r, Random.nextFloat() * h, speed, 0f, r)
            Side.RIGHT -> MenuBall(side, w + r, Random.nextFloat() * h, -speed, 0f, r)
            Side.TOP -> MenuBall(side, Random.nextFloat() * w, -r, 0f, speed, r)
            Side.BOTTOM -> MenuBall(side, Random.nextFloat() * w, h + r, 0f, -speed, r)
        }
    }

    LaunchedEffect(canvasWidth, canvasHeight) {
        if (canvasWidth <= 0f || canvasHeight <= 0f) return@LaunchedEffect
        if (balls.isEmpty()) {
            balls = listOf(
                spawnBall(Side.LEFT, canvasWidth, canvasHeight),
                spawnBall(Side.RIGHT, canvasWidth, canvasHeight),
                spawnBall(Side.TOP, canvasWidth, canvasHeight),
                spawnBall(Side.BOTTOM, canvasWidth, canvasHeight)
            )
        }
        var lastNanos = 0L
        while (true) {
            withFrameNanos { nanos ->
                if (lastNanos == 0L) {
                    lastNanos = nanos
                    return@withFrameNanos
                }
                val dtScale = ((nanos - lastNanos) / 16_666_667f).coerceIn(0.2f, 3f)
                lastNanos = nanos
                val w = canvasWidth
                val h = canvasHeight
                balls = balls.map { b ->
                    val nx = b.x + b.vx * dtScale
                    val ny = b.y + b.vy * dtScale
                    val out = nx < -b.r * 3 || nx > w + b.r * 3 || ny < -b.r * 3 || ny > h + b.r * 3
                    if (out) spawnBall(b.side, w, h) else b.copy(x = nx, y = ny)
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        canvasWidth = size.width
        canvasHeight = size.height
        val ballDiameterPx = (12f * density * 2f).roundToInt().coerceAtLeast(24)
        val half = ballDiameterPx / 2
        balls.forEach { b ->
            drawImage(
                image = cannonballBitmap,
                dstOffset = IntOffset((b.x - half).roundToInt(), (b.y - half).roundToInt()),
                dstSize = IntSize(ballDiameterPx, ballDiameterPx)
            )
        }
    }
}
