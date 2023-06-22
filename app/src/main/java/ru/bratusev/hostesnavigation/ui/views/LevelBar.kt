package ru.bratusev.hostesnavigation.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import ru.bratusev.hostesnavigation.R
import kotlin.math.roundToInt

class LevelBar(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var currentLevel = 1
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
        color = ContextCompat.getColor(context!!,R.color.brand)
        style = Paint.Style.FILL
    }

    private val fillPaintBack = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint().apply {
        color = ContextCompat.getColor(context!!,R.color.brand)
        style = Paint.Style.STROKE
        strokeWidth = pxToDp(2F)
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        this.textSize = pxToDp(36F)
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    private val textPaintWhite = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        this.textSize = pxToDp(36F)
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    fun pxToDp(px: Float): Float {
        val displayMetrics = context.resources.displayMetrics
        return (3.67*(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt().toFloat()).toFloat()
    }

    private val lBox = pxToDp(15f)
    private val tBox = pxToDp(10f)
    private val rBox = pxToDp(85f)
    private val bBox = pxToDp(240f)
    private val rxBox = pxToDp(10f)
    private val ryBox = pxToDp(10f)

    private val lCur = pxToDp(25f)
    private val tCur = pxToDp(100f)
    private val rCur = pxToDp(75f)
    private val bCur = pxToDp(150f)
    private val rxCur = pxToDp(10f)
    private val ryCur = pxToDp(10f)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRoundRect(lBox, tBox, rBox, bBox, rxBox, ryBox, fillPaintBack)
        canvas.drawRoundRect(lBox, tBox, rBox, bBox, rxBox, ryBox, strokePaint)
        canvas.drawRoundRect(lCur, tCur, rCur, bCur, rxCur, ryCur, fillPaint)
        drawLevel(canvas)
    }

    private fun drawLevel(canvas: Canvas) {
        val leftTextMargin = pxToDp(41f)
        val specLeftTextMargin = pxToDp(35f)
        val deltaMargin = pxToDp(12f)
        val topTextMargin = pxToDp(136f)

        if (currentLevel != maxLevel) {
            if (currentLevel + 1 < 0) canvas.drawText((currentLevel + 1).toString(), specLeftTextMargin, topTextMargin - textPaint.textSize * 2, textPaint)
            else if (currentLevel + 1  in 0..9) canvas.drawText((currentLevel + 1).toString(), leftTextMargin, topTextMargin - textPaint.textSize * 2, textPaint)
            else canvas.drawText((currentLevel + 1).toString(), leftTextMargin - deltaMargin, topTextMargin - textPaint.textSize * 2, textPaint)
        }

        if (currentLevel != minLevel) {
            if (currentLevel - 1 < 0) canvas.drawText((currentLevel - 1).toString(), specLeftTextMargin, topTextMargin + textPaint.textSize * 2, textPaint)
            else if (currentLevel - 1 in 0..9) canvas.drawText((currentLevel - 1).toString(), leftTextMargin, topTextMargin + textPaint.textSize * 2, textPaint)
            else canvas.drawText((currentLevel - 1).toString(), leftTextMargin - deltaMargin, topTextMargin + textPaint.textSize * 2, textPaint)
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
                Log.d("MyLog", "Move")
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
                /*Log.d("MyLog", event.y.toString())
                if(!isMove){
                    Log.d("MyLog", event.y.toString())
                    if(event.y < pxToDp(0f)) currentLevel++
                    if(event.y > pxToDp(136f) + textPaint.textSize * 2) currentLevel--
                    invalidate()
                    isUp = true
                }*/
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}