package com.simbirsoft.telegramconcurs.diagram

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.simbirsoft.telegramconcurs.diagram.diagram_data.LineDiagramData


class DiagramView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var xSize: Int = 0
    var ySize: Int = 0

    var yMax: Float = 0f
    var yMin: Float = 0f
    var yRange: Float = 0f

    var xMax: Float = 0f
    var xMin: Float = 0f
    var xRange: Float = 0f

    var defaultPaint = Paint()

    var graphicsScaleX: Float = 1f
    private var _data: LineDiagramData? = null

    var position: Float = 0f

    init {
        defaultPaint.color = Color.RED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        xSize = measuredHeight - paddingTop - paddingBottom
        ySize = measuredWidth - paddingLeft - paddingRight
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            _data?.let {
                for (item in it.values) {
                    drawPoint(translateX(item.x), translateY(item.y), canvas)
                }
            }
        }
        postInvalidateOnAnimation()
    }

    fun setData(data:LineDiagramData) {
        yMax = data.maximumValue
        yMin = data.minimumValue
        yRange = yMax - yMin

        xMin = data.values.first().x
        xMax = data.values.last().x
        xRange = xMax - xMin

        _data = data
        invalidate()
    }

    private fun translateX(x: Float) =
            paddingLeft + ((x - xMin)/xRange * xSize)

    private fun translateY(y: Float) =
            paddingTop + (ySize - ((y - yMin)/yRange) * ySize)

    fun setYScale(scale: Float) {
        graphicsScaleX = scale
        invalidate()
    }

    fun drawPoint(x: Float, y: Float, canvas: Canvas) {
        canvas.drawCircle(x,y,2f, defaultPaint)
    }
}