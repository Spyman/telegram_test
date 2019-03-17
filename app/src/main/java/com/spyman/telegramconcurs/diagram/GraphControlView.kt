package com.spyman.telegramconcurs.diagram

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class GraphControlView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    lateinit var diagramView: DiagramView

    fun attachToGraph(graph: DiagramView) {
        removeAllViews()
        diagramView = DiagramView(context)
        diagramView.setData(graph.getData())
        diagramView.onPositionChangeListener = object:OnValueChangeListener<Float> {
            override fun onChange(newValue: Float) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }

        addView(diagramView)
    }

}