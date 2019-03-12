package com.spyman.telegramconcurs.diagram

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.spyman.telegramconcurs.diagram.diagram_data.LineDiagramData


class DiagramView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    protected var xSize: Int = 0
    protected var ySize: Int = 0

    protected var yMax: Float = 0f
    protected var yMin: Float = 0f
    protected var yRange: Float = 0f

    protected var xMax: Float = 0f
    protected var xMin: Float = 0f
    protected var xRange: Float = 0f

    protected var graphicsScaleX: Float = 1f
    private var _data: List<LineDiagramData> = mutableListOf()

    protected lateinit var paints: List<Paint>

    protected var position: Float = 0f

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        xSize = measuredWidth - paddingTop - paddingBottom
        ySize = measuredHeight - paddingLeft - paddingRight
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {c ->
            _data.forEachIndexed { index, it ->
                for (i in 1 until it.values.size) {
                    c.drawLine(
                            translateX(it.values[i - 1].x),
                            translateY(it.values[i - 1].y),
                            translateX(it.values[i].x),
                            translateY(it.values[i].y),
                            paints[index]
                    )
                }
            }

        }
        postInvalidateOnAnimation()
    }

    fun setData(data:List<LineDiagramData>) {
        yMax = data.map { it.maximumValue }.max()!!
        yMin = data.map { it.minimumValue }.min()!!
        yRange = yMax - yMin

        xMin = data.map { it.values.first().x }.min()!!
        xMax = data.map { it.values.last().x }.max()!!
        xRange = xMax - xMin

        _data = data

        paints = data.map {
            Paint().apply {
                this.color = it.color
                this.strokeWidth = 5f
            }
        }

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
}