package com.spyman.telegramconcurs.diagram

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.spyman.telegramconcurs.R
import com.spyman.telegramconcurs.diagram.diagram_data.LineDiagramData
import kotlinx.android.synthetic.main.legend_view.view.*

class LegendView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), OnValueChangeListener<List<LineDiagramData>> {
    init {
        addView(LayoutInflater.from(context).inflate(R.layout.legend_view, this, false))
    }

    fun attachToGraph(diagramView: DiagramView) {
        diagramView.setOnDataChangeListener(this)
    }

    override fun onChange(newValue: List<LineDiagramData>) {
        recycler.adapter = LegendAdapter(newValue)
    }
}