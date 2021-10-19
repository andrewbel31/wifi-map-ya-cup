package com.andreibelous.wifimap.view

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.andreibelous.wifimap.cast
import com.andreibelous.wifimap.data.Signal
import kotlin.math.abs

class HeatMap
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        setWillNotDraw(false)
    }

    private var minLevel = Integer.MAX_VALUE
    private var maxLevel = Integer.MIN_VALUE

    private var minX = Float.MAX_VALUE
    private var maxX = Float.MIN_VALUE

    private var minY = Float.MAX_VALUE
    private var maxY = Float.MIN_VALUE

    private var rangeX = 0f
    private var rangeY = 0f
    private var rangeLevel = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val evaluator = ArgbEvaluator()

    private fun calculateParams() {
        for (signal in data) {
            val level = signal.level

            if (level > maxLevel) {
                maxLevel = level
            }

            if (level < minLevel) {
                minLevel = level
            }

            val x = signal.point.x
            val y = signal.point.y

            if (x < minX) {
                minX = x
            }

            if (x > maxX) {
                maxX = x
            }

            if (y < minY) {
                minY = y
            }

            if (y > maxY) {
                maxY = y
            }
        }

        rangeX = abs(maxX - minX)
        rangeY = abs(maxY - minY)
        rangeLevel = abs(maxLevel - minLevel)
    }

    private var data: List<Signal> = emptyList()
        set(value) {
            field = value
            invalidate()
            calculateParams()
        }

    fun bind(data: List<Signal>) {
        this.data = data
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        data.forEach {
            val fraction = (it.level.toFloat() - minLevel) / rangeLevel
            paint.color = evaluator.evaluate(fraction, Color.RED, Color.GREEN).cast()

            canvas.drawCircle(
                (it.point.x - minX) / rangeX * width,
                (it.point.y - minY) / rangeY * height,
                RADIUS,
                paint
            )
        }
    }

    private companion object {

        private const val RADIUS = 40f
    }
}