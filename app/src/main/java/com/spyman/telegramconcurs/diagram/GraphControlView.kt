package com.spyman.telegramconcurs.diagram

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.spyman.telegramconcurs.R

open class GraphControlView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    lateinit var diagramView: DiagramView
    var window: ImageView? = null
    var graph: DiagramView? = null

    fun attachToGraph(graph: DiagramView) {
        removeAllViews()
        this.graph = graph

        diagramView = DiagramView(context)
        diagramView.setData(graph.getData())
        diagramView.isXAxisVisible = false
        diagramView.isYAxisVisible = false
        graph.onPositionChangeListener = object:OnValueChangeListener<Float> {
            override fun onChange(newValue: Float) {
                window?.x = -graph.position/graph.graphicsScaleX*measuredWidth.toFloat()/graph.xSize.toFloat()
            }
        }

        addView(diagramView)
        window = ImageView(context)
        window?.post {
            window?.setImageResource(R.drawable.window)
            val layoutParams = RelativeLayout.LayoutParams(Math.round(measuredWidth.toFloat()/graph.graphicsScaleX.toFloat()*measuredWidth.toFloat()/graph.xSize.toFloat()), ViewGroup.LayoutParams.MATCH_PARENT)

            window?.layoutParams = layoutParams
            window?.x = -graph.position/graph.graphicsScaleX
            window?.requestLayout()
        }
        addView(window)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        window?.measure(widthMeasureSpec, heightMeasureSpec)
    }
}