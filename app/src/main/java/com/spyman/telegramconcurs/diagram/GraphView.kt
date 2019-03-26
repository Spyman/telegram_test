package com.spyman.telegramconcurs.diagram

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import android.widget.OverScroller
import android.widget.TextView
import com.spyman.telegramconcurs.R
import com.spyman.telegramconcurs.diagram.diagram_data.DiagramValue
import com.spyman.telegramconcurs.diagram.diagram_data.LineDiagramData
import com.spyman.telegramconcurs.extentions.getColorFromAttr


open class GraphView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var data: List<LineDiagramData> = mutableListOf()


    protected val defaultXAxisSize = 84 // todo change to dimen
    var xSize: Int = 0 // todo make private
    var minScale = 0.2f

    protected var ySize: Int = 0
    protected var yMax: Float = 0f
    protected var yMin: Float = 0f

    protected var yRange: Float = 0f
    protected var xMax: Float = 0f
    protected var xMin: Float = 0f

    protected var xRange: Float = 0f
    protected var graphicsScaleX: Float = 1f
    set(value) {
        field = value
        onScaleChagneListener?.onChange(value)
    }


    protected lateinit var paints: List<Paint>

    var position: Float = 0f // todo make private
    set(value) {
        field = value
        onPositionChangeListener?.onChange(value)
    }

    var dinamicMinValue = false
    var dinamicMaxValue = true

    val inScreenList = mutableListOf<MutableList<DiagramValue>>()
    var axisPaint = Paint().apply { color = Color.GRAY; strokeWidth = 1f; isAntiAlias = true } // todo width to dimen
    val backgroundPaint = Paint().apply { color = context.getColorFromAttr(android.R.attr.windowBackground); strokeWidth = 1f; isAntiAlias = true }

    var xAxisHeight = defaultXAxisSize
    var xAxisValueFormatter = DefaultValueFormatter()
    var isXAxisVisible = true
    set(value) {
        field = value
        calculateSizes()
    }
    var isYAxisVisible = true

    var yAxisCount = 5
    var onPositionChangeListener: OnValueChangeListener<Float>? = null
    var onScaleChagneListener: OnValueChangeListener<Float>? = null
    private var onDataChangedListener: OnValueChangeListener<List<LineDiagramData>>? = null
    private var selectedValues:List<DiagramValue>? = null
    var bubbleBitmap: Bitmap? = null
    var bubblePositionX = 0f
    var bubblePositionY = 0f

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
            if (isValueInsideBounds(nextPosition)) {
                position = nextPosition
            }
            postInvalidateOnAnimation()
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            e?.let {
                val newSelectedValues = findNested(it.x)
                if (newSelectedValues.equals(selectedValues)) {
                    selectedValues = null
                    hideBubble()
                } else {
                    selectedValues = newSelectedValues
                    showBubble()
                }
                invalidate()
            }
            return super.onSingleTapUp(e)
        }

    }, Handler())

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        calculateSizes()
    }

    private fun calculateSizes() {
        xSize = measuredWidth - paddingTop - paddingBottom
        ySize = measuredHeight - paddingLeft - paddingRight - if (isXAxisVisible) {defaultXAxisSize} else {0}
    }

    protected fun isValueInsideBounds(value: Float) =
            !isValueOutsideBoundRight(value) && !isValueOutsideBoundLeft(value)

    protected fun isValueOutsideBoundLeft(value: Float) =
            value > graphLeft()

    protected fun isValueOutsideBoundRight(value: Float) =
            positionRight(-value) > graphRight()

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {c ->
            if (data.isNotEmpty()) {
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
                drawXAxis(c)
                drawYAxis(c)
            }

            selectedValues?.let {
                for (i in 0 until it.size) {
                    if (!data[i].visible) {
                        continue
                    }
                    val radius = 12f // todo change to dimention
                    val centerX = translateX(it[i].x)
                    val centerY = translateY(it[i].y)
                    c.drawCircle(centerX, centerY, radius, paints[i])
                    c.drawCircle(centerX, centerY, radius/2, backgroundPaint)
                }
            }

            bubbleBitmap?.let {
                val lineX = translateX(bubblePositionX)
                canvas.drawLine(lineX, translateY(yMin), lineX, translateY(yMax), axisPaint)
                val bubbleMovedPosition = lineX - (bubbleBitmap!!.width * 1/3)
                canvas.drawBitmap(it, bubbleMovedPosition, bubblePositionY, null)
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
        if (!isEnabled) { return false }
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
            it.addVisibilityChangeListener(object : OnValueChangeListener<Boolean> {
                override fun onChange(newValue: Boolean) {
                    onLineVisibilityChanged(it)
                }
            })
        }

        paints = data.map {
            Paint().apply {
                this.color = it.color
                this.strokeWidth = 5f
                this.isAntiAlias = true
            }
        }

        this.data = data

        onDataChangedListener?.onChange(data)
        invalidate()
    }

    private fun translateX(x: Float) =
            paddingLeft + (((x - xMin)/xRange * xSize) * graphicsScaleX + position)

    private fun translateY(y: Float) =
            paddingTop + (ySize - ((y - yMin)/yRange) * ySize) + if (isXAxisVisible) { defaultXAxisSize/2 } else { 0 }

    private fun calculateOnScreenRangeItems() {
        for (i in 0 until data.size) {
            var left: DiagramValue = data[i].values.first()
            var right: DiagramValue = data[i].values.last()
            inScreenList[i].clear()
            if (!data[i].visible) {
                continue
            }
            for (j in 0 until data[i].values.size) {
                val translatedValue = translateX(data[i].values[j].x)

                if (translatedValue <= 0) {
                    left = data[i].values[j]
                }

                if (translatedValue > 0 && translatedValue < xSize + paddingLeft + paddingRight) {
                    inScreenList[i].add(data[i].values[j])
                }

                if (translatedValue > xSize + paddingLeft + paddingRight ) {
                    right = data[i].values[j]
                    break
                }
            }

            inScreenList[i].add(0, left)
            inScreenList[i].add(right)
        }
    }

    private fun calculateOnScreenMax(): Float { // todo may be faster
        var max = data.first().values.first().y
        for (line in data) {
            if (!line.visible) {
                continue
            }
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
        val newScale = if (scale < minScale) {minScale} else {scale}
        val absPos = position / graphicsScaleX
        graphicsScaleX = newScale
        position = absPos * newScale
        invalidate()
    }

    fun drawXAxis(canvas: Canvas) {
        if (isXAxisVisible) {
            canvas.drawLine(translateX(xMin), translateY(yMin), translateX(xMax), translateY(yMin), axisPaint)
        }
    }

    fun drawYAxis(canvas: Canvas) {
        if (!isYAxisVisible) {
            return
        }
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
        DiagramState(position/xSize, graphicsScaleX, data, dinamicMinValue, super.onSaveInstanceState())

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

    fun getData() = data

    fun updatePosition(x: Float) {
        position = if (isValueOutsideBoundRight(x)) {
            positionRight(-graphRight())
        } else if (isValueOutsideBoundLeft(x)) {
            graphLeft().toFloat()
        } else {
            x
        }
        postInvalidateOnAnimation()
    }

    fun abortScroll() {
        scroller.forceFinished(true)
    }

    fun getXScale() = graphicsScaleX

    fun setOnDataChangeListener(onDataChangedListener: OnValueChangeListener<List<LineDiagramData>>) {
        onDataChangedListener.onChange(data)
        this.onDataChangedListener = onDataChangedListener
    }

    fun findNested(windowX: Float) =
        data.map {
            var minimal = Float.POSITIVE_INFINITY
            var minimalPos = 0
            it.let {
                it.values.forEachIndexed { i, value ->
                    Math.abs(translateX(value.x) - windowX).let {
                        if (it < minimal) {
                            minimal = it
                            minimalPos = i
                        }
                    }
                }

                return@map it.values[minimalPos]
            }
        }

    fun showBubble() {
        val bubbleView = LayoutInflater.from(context).inflate(R.layout.bubble_view, rootView as ViewGroup, false)
        bubbleView.findViewById<TextView>(R.id.data_text).setText("Text from xaxis\nformatter")
        val contentView = bubbleView.findViewById<LinearLayout>(R.id.values)
        selectedValues?.forEachIndexed { i, it ->
            val view = LayoutInflater.from(context).inflate(R.layout.bubble_item, contentView, false)
            view.findViewById<TextView>(R.id.value).apply {
                text = Math.round(it.y).toString()
                setTextColor(data[i].color) // todo change to strong logic
            }
            view.findViewById<TextView>(R.id.name).apply {
                text = data[i].name
                setTextColor(data[i].color)
            }
            contentView.addView(view)
        }
        bubbleView.measure(0, 0)
        val bitmap = Bitmap.createBitmap(bubbleView.measuredWidth, bubbleView.measuredHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        bubbleView.layout(0, 0, bubbleView.measuredWidth, bubbleView.measuredHeight)
        bubbleView.draw(c)
        bubbleBitmap = bitmap
        bubblePositionX = calculateBubblePositionX()
    }

    fun calculateBubblePositionX(): Float {
        var sum = 0f
        selectedValues?.filterIndexed {i, it -> data[i].visible }?.forEach {
            sum += it.x
        }
        return sum / (selectedValues?.size?: 1)
    }

    fun hideBubble() {
        bubbleBitmap = null
    }

    fun onLineVisibilityChanged(item: LineDiagramData) {
        bubbleBitmap?.let {
            showBubble()
        }
        invalidate()
    }
}