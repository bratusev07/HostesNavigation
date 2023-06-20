package ru.bratusev.hostesnavigation.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class LevelBar(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var currentLevel = 3
    private var maxLevel = 10
    private var minLevel = 0
    private val duration = 60
    private var deltaY = 0

    fun getLevel(): Int {
        return currentLevel
    }

    fun setLevelRange(minLevel: Int = 0, maxLevel: Int = 10) {
        this.minLevel = minLevel
        this.maxLevel = maxLevel
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    private val fillPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        this.textSize = 30F
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRoundRect(15f, 10f, 85f, 200f, 10f, 10f, strokePaint)
        canvas.drawRoundRect(25f, 80f, 75f, 130f, 10f, 10f, fillPaint)
        drawLevel(canvas)
    }

    private fun drawLevel(canvas: Canvas) {
        val leftTextMargin = 42f
        val deltaMargin = 12f
        val topTextMargin = 116f

        if (currentLevel != maxLevel) {
            if (currentLevel + 1  in 0..9) canvas.drawText(
                (currentLevel + 1).toString(),
                leftTextMargin,
                topTextMargin - textPaint.textSize * 2,
                textPaint
            )
            else canvas.drawText(
                (currentLevel + 1).toString(),
                leftTextMargin - deltaMargin,
                topTextMargin - textPaint.textSize * 2,
                textPaint
            )
        }
        if (currentLevel + 0 in 0..9) canvas.drawText(
            (currentLevel + 0).toString(),
            leftTextMargin,
            topTextMargin,
            textPaint
        )
        else canvas.drawText(
            (currentLevel + 0).toString(),
            leftTextMargin - deltaMargin,
            topTextMargin,
            textPaint
        )

        if (currentLevel != minLevel) {
            if (currentLevel - 1 in 0..9) canvas.drawText(
                (currentLevel - 1).toString(),
                leftTextMargin,
                topTextMargin + textPaint.textSize * 2,
                textPaint
            )
            else canvas.drawText(
                (currentLevel - 1).toString(),
                leftTextMargin - deltaMargin,
                topTextMargin + textPaint.textSize * 2,
                textPaint
            )

        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                deltaY = event.y.toInt()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if ((event.y.toInt() - deltaY) > duration) {
                    if (currentLevel < maxLevel) {
                        currentLevel++
                        deltaY = event.y.toInt()
                        invalidate()
                    }
                }
                if ((deltaY - event.y.toInt()) > duration) {
                    if (currentLevel > minLevel) {
                        --currentLevel
                        deltaY = event.y.toInt()
                        invalidate()
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}