package com.spyman.telegramconcurs.diagram

import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyman.telegramconcurs.R
import com.spyman.telegramconcurs.diagram.diagram_data.LineDiagramData
import android.support.v7.widget.AppCompatCheckBox
import android.widget.CheckBox
import android.widget.CompoundButton


class LegendAdapter(private val data: List<LineDiagramData>): RecyclerView.Adapter<LegendAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) =
            ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.legend_item, p0, false))

    override fun getItemCount() =
            data.size

    override fun onBindViewHolder(viewHolder: ViewHolder, index: Int) =
            viewHolder.bind(data[index])


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val checkbox = itemView.findViewById<AppCompatCheckBox>(R.id.checkbox)

        fun bind(data: LineDiagramData) {
            checkbox.text = data.name
            checkbox.supportButtonTintList = ColorStateList(
                    arrayOf(intArrayOf(-android.R.attr.state_checked), // unchecked
                            intArrayOf(android.R.attr.state_checked)  // checked
                    ),
                    intArrayOf(data.color, data.color)
            )
            checkbox.isChecked = data.visible
            checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                data.visible = isChecked
            }
        }
    }
}