package com.simbirsoft.telegramconcurs.diagram

import android.content.Context
import android.graphics.Canvas
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

    var xMax: Float = 0f
    var xMin: Float = 0f

    var graphScaleY: Float = 1f
    private var _data: LineDiagramData? = null

    var position: Float = 0f

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        xSize = measuredHeight - paddingTop - paddingBottom
        ySize = measuredWidth - paddingLeft - paddingRight
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        _data?.let {
            for (item in it.values) {

            }
        }
        postInvalidateOnAnimation()
    }

    fun setData(data:LineDiagramData) {
        yMax = data.maximumValue
        yMin = data.minimumValue

        xMin = data.values.first().x
        xMax = data.values.last().x

        _data = data
        invalidate()
    }

    private fun translateX(x: Float) {

    }

    private fun translateY(y: Float) = y

    fun setYScale(scale: Float) {
        graphScaleY = scale
        invalidate()
    }
}