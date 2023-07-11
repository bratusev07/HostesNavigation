package ru.bratusev.hostesnavigation.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import ru.bratusev.hostesnavigation.R

class LevelBar(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var currentLevel = 1
    private var maxLevel = 10
    private var minLevel = 0
    private val duration = 60
    private var deltaY = 0

    private var width = 0
    private var height = 0

    private var lBox = 5f
    private var tBox = 5f
    private var rBox = 0f
    private var bBox = 0f
    private var rxBox = 10f / 1.86f
    private var ryBox = 10f / 1.86f

    private var lCur = 25f
    private var tCur = 100f
    private var rCur = 75f
    private var bCur = 150f
    private var rxCur = 10f / 1.86f
    private var ryCur = 10f / 1.86f

    fun getLevel(): Int {
        return currentLevel
    }

    fun setLevelRange(minLevel: Int = 0, maxLevel: Int = 10) {
        this.minLevel = minLevel
        this.maxLevel = maxLevel
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = MeasureSpec.getSize(widthMeasureSpec)
        height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
        setSize()
    }

    private fun setSize() {
        rBox = width.toFloat() / 1.86f
        bBox = height.toFloat() / 1.86f

        lCur = width.toFloat() / 10f
        tCur = height.toFloat() / 2.9f - height.toFloat()/7.3f
        rCur = width.toFloat() - width.toFloat()/2 - 1f
        bCur = height.toFloat() / 2.9f
    }

    private val fillPaint = Paint().apply {
        color = ContextCompat.getColor(context!!, R.color.brand)
        style = Paint.Style.FILL
    }

    private val fillPaintBack = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint().apply {
        color = ContextCompat.getColor(context!!, R.color.brand)
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        this.textSize = context?.resources?.displayMetrics?.scaledDensity!!*16*1.5f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    private val textPaintWhite = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        this.textSize = context?.resources?.displayMetrics?.scaledDensity!!*16*1.5f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRoundRect(lBox, tBox, rBox, bBox, rxBox, ryBox, fillPaintBack)
        canvas.drawRoundRect(lBox, tBox, rBox, bBox, rxBox, ryBox, strokePaint)
        canvas.drawRoundRect(lCur, tCur, rCur, bCur, rxCur, ryCur, fillPaint)
        drawLevel(canvas)
    }

    private fun drawLevel(canvas: Canvas) {
        val leftTextMargin = lCur*2.1f
        val specLeftTextMargin = height / 16f
        val deltaMargin = height / 36f
        val topTextMargin = tCur + textPaint.textSize

        if (currentLevel != maxLevel) {
            if (currentLevel + 1 < 0) canvas.drawText((currentLevel + 1).toString(), specLeftTextMargin, topTextMargin - textPaint.textSize * 1.6f, textPaint)
            else if (currentLevel + 1 in 0..9) canvas.drawText((currentLevel + 1).toString(), leftTextMargin, topTextMargin - textPaint.textSize * 1.6f, textPaint)
            else canvas.drawText((currentLevel + 1).toString(), leftTextMargin - deltaMargin, topTextMargin - textPaint.textSize * 1.6f, textPaint)
        }

        if (currentLevel != minLevel) {
            if (currentLevel - 1 < 0) canvas.drawText((currentLevel - 1).toString(), specLeftTextMargin, topTextMargin + textPaint.textSize * 1.6f, textPaint)
            else if (currentLevel - 1 in 0..9) canvas.drawText((currentLevel - 1).toString(), leftTextMargin, topTextMargin + textPaint.textSize * 1.6f, textPaint)
            else canvas.drawText((currentLevel - 1).toString(), leftTextMargin - deltaMargin, topTextMargin + textPaint.textSize * 1.6f, textPaint)
        }

        if (currentLevel + 0 in 0..9) canvas.drawText((currentLevel + 0).toString(), leftTextMargin, topTextMargin, textPaintWhite)
        else if (currentLevel + 0 < 0) canvas.drawText((currentLevel + 0).toString(), specLeftTextMargin, topTextMargin, textPaintWhite)
        else canvas.drawText((currentLevel + 0).toString(), leftTextMargin - deltaMargin, topTextMargin, textPaintWhite)
    }

    var isMove: Boolean = false
    var isUp: Boolean = false
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isUp = false
                isMove = false
                deltaY = event.y.toInt()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                isMove = true
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