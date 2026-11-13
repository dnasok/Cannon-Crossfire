package com.baabearianstudios.cannoncrossfire.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.baabearianstudios.cannoncrossfire.GameUiState
import com.baabearianstudios.cannoncrossfire.R
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun GameArenaCanvas(
    uiState: GameUiState,
    onArenaSizeChanged: (dpWidth: Float, dpHeight: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val playerBitmap = ImageBitmap.imageResource(id = R.drawable.player)
    val enemyBitmap = ImageBitmap.imageResource(id = R.drawable.enemy)
    val cannonballBitmap = ImageBitmap.imageResource(id = R.drawable.cannonball)
    val coinBitmap = ImageBitmap.imageResource(id = R.drawable.coin)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { intSize ->
                val dpW = intSize.width / density.density
                val dpH = intSize.height / density.density
                onArenaSizeChanged(dpW, dpH)
            }
    ) {
        val pxScale = this.density

        // 1. Kill Zone Red Perimeter Border (14dp stroke)
        val borderStrokePx = 14.dp.toPx()
        val halfStroke = borderStrokePx / 2f
        drawRect(
            color = Color(0xFFEF4444),
            topLeft = Offset(halfStroke, halfStroke),
            size = Size(size.width - borderStrokePx, size.height - borderStrokePx),
            style = Stroke(width = borderStrokePx)
        )

        // 2. Cannons along perimeter
        uiState.cannons.forEach { cannon ->
            val cx = cannon.x * pxScale
            val cy = cannon.y * pxScale
            val rPx = (cannon.radius * pxScale).roundToInt()
            val dPx = rPx * 2
            rotate(degrees = cannon.angle, pivot = Offset(cx, cy)) {
                drawImage(
                    image = enemyBitmap,
                    dstOffset = IntOffset((cx - rPx).roundToInt(), (cy - rPx).roundToInt()),
                    dstSize = IntSize(dPx, dPx)
                )
            }
        }

        // 3. Bonus Coin with gentle pulse
        uiState.coin?.let { coin ->
            val pulse = 1f + 0.08f * sin(uiState.gameTimeMs / 180f)
            val cx = coin.x * pxScale
            val cy = coin.y * pxScale
            val rPx = (coin.radius * pxScale * pulse).roundToInt()
            val dPx = rPx * 2
            drawImage(
                image = coinBitmap,
                dstOffset = IntOffset((cx - rPx).roundToInt(), (cy - rPx).roundToInt()),
                dstSize = IntSize(dPx, dPx)
            )
        }

        // 4. Cannonballs
        uiState.cannonballs.forEach { cb ->
            val bx = cb.x * pxScale
            val by = cb.y * pxScale
            val rPx = (cb.radius * pxScale).roundToInt()
            val dPx = rPx * 2
            drawImage(
                image = cannonballBitmap,
                dstOffset = IntOffset((bx - rPx).roundToInt(), (by - rPx).roundToInt()),
                dstSize = IntSize(dPx, dPx)
            )
        }

        // 5. Player
        val p = uiState.player
        val px = p.x * pxScale
        val py = p.y * pxScale
        val pRPx = (p.radius * pxScale).roundToInt()
        val pDPx = pRPx * 2
        drawImage(
            image = playerBitmap,
            dstOffset = IntOffset((px - pRPx).roundToInt(), (py - pRPx).roundToInt()),
            dstSize = IntSize(pDPx, pDPx)
        )
    }
}
