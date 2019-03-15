package com.spyman.telegramconcurs.diagram

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.util.AttributeSet
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
    protected var graphicsScaleX: Float = 10f

    protected lateinit var paints: List<Paint>

    protected var position: Float = 0f
    var dinamicSize = true

    val inScreenList = mutableListOf<MutableList<DiagramValue>>()

    //protected val positionController = PositionController()
    protected val scroller = OverScroller(context)
    protected val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            // Initiates the decay phase of any active edge effects.
            //releaseEdgeEffects()
            // Aborts any active scroll animations and invalidates.
            scroller.forceFinished(true)
            postInvalidateOnAnimation()
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            scroller.forceFinished(true)
            // Begins the animation
            scroller.fling(
                    // Current scroll position
                    Math.round(position),
                    0,
                    Math.round(velocityX),
                    0,
                    /*
                     * Minimum and maximum scroll positions. The minimum scroll
                     * position is generally zero and the maximum scroll position
                     * is generally the content size less the screen size. So if the
                     * content width is 1000 pixels and the screen width is 200
                     * pixels, the maximum scroll offset should be 800 pixels.
                     */
                    Math.round((-xSize * graphicsScaleX) + xSize), 0,
                    Int.MIN_VALUE, Int.MAX_VALUE,
                    // The edges of the content. This comes into play when using
                    // the EdgeEffect class to draw "glow" overlays.
                    Math.round(xSize * 0.4f),
                    ySize
            )
            // Invalidates to trigger computeScroll()
            postInvalidateOnAnimation()
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            position -= distanceX
            postInvalidateOnAnimation()
            return true
        }

    }, Handler())

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        xSize = measuredWidth - paddingTop - paddingBottom
        ySize = measuredHeight - paddingLeft - paddingRight
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {c ->
            if (_data.isNotEmpty()) {
                calculateOnScreenRangeItems()
                if (dinamicSize) {
                    yMin = calculateOnScreenMin()
                    yMax = calculateOnScreenMax()
                    yRange = yMax - yMin
                }
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

    private fun calculateOnScreenMax(): Float {
        var max = inScreenList.first().first().y
        for (line in inScreenList) {
            val maxInLine = line.maxBy { it.y }!!.y
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

    fun setYScale(scale: Float) {
        graphicsScaleX = scale
        invalidate()
    }
}