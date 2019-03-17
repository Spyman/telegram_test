package com.spyman.telegramconcurs.diagram

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import com.spyman.telegramconcurs.diagram.diagram_data.DiagramValue
import com.spyman.telegramconcurs.diagram.diagram_data.LineDiagramData


open class DiagramView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var _data: List<LineDiagramData> = mutableListOf()
    protected var xSize: Int = 0

    protected var ySize: Int = 0
    protected var yMax: Float = 0f
    protected var yMin: Float = 0f

    protected var yRange: Float = 0f
    protected var xMax: Float = 0f
    protected var xMin: Float = 0f

    protected var xRange: Float = 0f
    protected var graphicsScaleX: Float = 1f

    protected lateinit var paints: List<Paint>

    protected var position: Float = 0f
    set(value) {
        field = value
        onPositionChangeListener?.onChange(value)
    }

    var dinamicMinValue = false
    var dinamicMaxValue = true

    val inScreenList = mutableListOf<MutableList<DiagramValue>>()
    var axisPaint = Paint().apply { color = Color.GRAY; strokeWidth = 1f; isAntiAlias = true }

    var xAxisHeight = 100
    var xAxisValueFormatter = DefaultValueFormatter()

    var yAxisCount = 5
    var onPositionChangeListener: OnValueChangeListener<Float>? = null

    //protected val positionController = PositionController()
    protected val scroller = OverScroller(context)
    protected val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            scroller.forceFinished(true)
            postInvalidateOnAnimation()
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            scroller.forceFinished(true)
            scroller.fling(
                    Math.round(position),
                    0,
                    Math.round(velocityX),
                    0,
                    Math.round((-xSize * graphicsScaleX) + xSize), 0,
                    Int.MIN_VALUE, Int.MAX_VALUE,
                    0,
                    ySize
            )
            postInvalidateOnAnimation()
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            val nextPosition = position - distanceX
            if (positionRight(-nextPosition) <= graphRight() && position - distanceX <= graphLeft()) {
                position -= distanceX
            }
            postInvalidateOnAnimation()
            return true
        }

    }, Handler())

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        xSize = measuredWidth - paddingTop - paddingBottom
        ySize = measuredHeight - paddingLeft - paddingRight - xAxisHeight
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawColor(Color.DKGRAY)
        canvas?.let {c ->
            if (_data.isNotEmpty()) {
                calculateOnScreenRangeItems()
                if (dinamicMinValue) {
                    yMin = calculateOnScreenMin()
                }
                if (dinamicMaxValue) {
                    yMax = calculateOnScreenMax()
                }
                yRange = yMax - yMin
                inScreenList.forEachIndexed { index, it ->
                    for (i in 1 until it.size) {
                        c.drawLine(
                                translateX(it[i - 1].x),
                                translateY(it[i - 1].y),
                                translateX(it[i].x),
                                translateY(it[i].y),
                                paints[index]
                        )
                    }
                }
                drawXAxis(canvas)
                drawYAxis(canvas)
            }
        }

        if (!scroller.isFinished) {
            scroller.computeScrollOffset()
            position = scroller.currX.toFloat()
            postInvalidateOnAnimation()
        }
    }



    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        performClick()
        gestureDetector.onTouchEvent(event)
        return true
    }


    fun setData(data:List<LineDiagramData>) {
        yMax = data.map { it.maximumValue }.max()!!
        yMin = data.map { it.minimumValue }.min()!!
        yRange = yMax - yMin

        xMin = data.map { it.values.first().x }.min()!!
        xMax = data.map { it.values.last().x }.max()!!
        xRange = xMax - xMin

        inScreenList.clear()
        data.forEach {
            inScreenList.add(mutableListOf())
        }

        paints = data.map {
            Paint().apply {
                this.color = it.color
                this.strokeWidth = 5f
                this.isAntiAlias = true
            }
        }

        _data = data

        invalidate()
    }

    private fun translateX(x: Float) =
            paddingLeft + (((x - xMin)/xRange * xSize) * graphicsScaleX + position)

    private fun translateY(y: Float) =
            paddingTop + (ySize - ((y - yMin)/yRange) * ySize)

    private fun calculateOnScreenRangeItems() {
        for (i in 0 until _data.size) {
            var left: DiagramValue = _data[i].values.first()
            var right: DiagramValue = _data[i].values.last()
            inScreenList[i].clear()
            for (j in 0 until _data[i].values.size) {
                val translatedValue = translateX(_data[i].values[j].x)

                if (translatedValue <= 0) {
                    left = _data[i].values[j]
                }

                if (translatedValue > 0 && translatedValue < xSize + paddingLeft + paddingRight) {
                    inScreenList[i].add(_data[i].values[j])
                }

                if (translatedValue > xSize + paddingLeft + paddingRight ) {
                    right = _data[i].values[j]
                    break
                }
            }

            inScreenList[i].add(0, left)
            inScreenList[i].add(right)
        }
    }

    private fun calculateOnScreenMax(): Float { // todo may be faster
        var max = _data.first().values.first().y
        for (line in _data) {
            var maxInLine = line.values.first().y
            for (value in line.values) {
                val y = value.y
                val multiper = translateX(value.x).let {
                    if (it > xSize) {
                        return@let (1 - ((it - xSize)/xSize))
                    }
                    if (it < paddingLeft) {
                        return@let 1 - ((-it)/xSize)
                    }
                    1f
                }

                if (y * multiper > maxInLine) {
                    maxInLine = y * multiper
                }
            }
            if (maxInLine > max) {
                max = maxInLine
            }
        }
        return max
    }

    private fun calculateOnScreenMin(): Float {
        var min = inScreenList.first().first().y
        for (line in inScreenList) {
            val minInLine = line.minBy { it.y }!!.y
            if (minInLine < min) {
                min = minInLine
            }
        }
        return min
    }

    fun setXScale(scale: Float) {
        graphicsScaleX = scale
        invalidate()
    }

    fun drawXAxis(canvas: Canvas) {
        canvas.drawLine(translateX(xMin), translateY(yMin), translateX(xMax), translateY(yMin), axisPaint)
    }

    fun drawYAxis(canvas: Canvas) {
        var startValue = yMin
        val addValue = yRange/yAxisCount
        for (i in 0..yAxisCount) {
            canvas.drawLine(translateX(xMin), translateY(startValue), translateX(xMax), translateY(startValue), axisPaint)
            startValue += addValue
        }
    }

    private fun screenLeftSide() = -position

    private fun screenRightSide() = -position - xSize

    private fun graphLeft() = paddingLeft

    private fun graphRight() = paddingLeft + xSize * graphicsScaleX

    private fun positionRight(position: Float) =
            position + xSize

    override fun onSaveInstanceState(): Parcelable? =
        DiagramState(position/xSize, graphicsScaleX, _data, dinamicMinValue, super.onSaveInstanceState())

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is DiagramState) {
            super.onRestoreInstanceState(state)
            return
        }

        setData(state.data)
        dinamicMinValue = state.dinamicSize
        //position = state.position*(measuredWidth - paddingTop - paddingBottom)
        graphicsScaleX = state.graphScaleX
        super.onRestoreInstanceState(state.superState)
    }

    fun getData() = _data
}