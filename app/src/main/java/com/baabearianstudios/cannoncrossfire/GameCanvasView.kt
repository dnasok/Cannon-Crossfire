package com.baabearianstudios.cannoncrossfire

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class GameCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var engine: GameEngine? = null

    private val playerBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.player)
    private val enemyBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.enemy)
    private val cannonballBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.cannonball)
    private val coinBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.coin)

    private val redBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF4444")
        style = Paint.Style.STROKE
        strokeWidth = 14f
    }

    private val srcRect = Rect()
    private val dstRectF = RectF()

    fun bindEngine(gameEngine: GameEngine) {
        this.engine = gameEngine
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        engine?.updateDimensions(w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val eng = engine ?: return

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        // 1. Draw Kill Zone border (Red stroke, width 14px)
        canvas.drawRect(7f, 7f, w - 7f, h - 7f, redBorderPaint)

        // 2. Draw Cannons
        for (c in eng.cannons) {
            canvas.save()
            canvas.translate(c.x, c.y)
            canvas.rotate(c.angle)
            dstRectF.set(-c.radius, -c.radius, c.radius, c.radius)
            srcRect.set(0, 0, enemyBitmap.width, enemyBitmap.height)
            canvas.drawBitmap(enemyBitmap, srcRect, dstRectF, null)
            canvas.restore()
        }

        // 3. Draw Coin
        eng.coin?.let { coin ->
            dstRectF.set(coin.x - coin.radius, coin.y - coin.radius, coin.x + coin.radius, coin.y + coin.radius)
            srcRect.set(0, 0, coinBitmap.width, coinBitmap.height)
            canvas.drawBitmap(coinBitmap, srcRect, dstRectF, null)
        }

        // 4. Draw Cannonballs
        for (cb in eng.cannonballs) {
            dstRectF.set(cb.x - cb.radius, cb.y - cb.radius, cb.x + cb.radius, cb.y + cb.radius)
            srcRect.set(0, 0, cannonballBitmap.width, cannonballBitmap.height)
            canvas.drawBitmap(cannonballBitmap, srcRect, dstRectF, null)
        }

        // 5. Draw Player
        val p = eng.player
        dstRectF.set(p.x - p.radius, p.y - p.radius, p.x + p.radius, p.y + p.radius)
        srcRect.set(0, 0, playerBitmap.width, playerBitmap.height)
        canvas.drawBitmap(playerBitmap, srcRect, dstRectF, null)
    }
}
