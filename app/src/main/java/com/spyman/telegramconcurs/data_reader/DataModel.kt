package com.spyman.telegramconcurs.data_reader

import android.graphics.Color
import com.spyman.telegramconcurs.diagram.diagram_data.DiagramValue
import com.spyman.telegramconcurs.diagram.diagram_data.LineDiagramData

class DataModel {
    val columns: List<List<String>>? = null
    val types: HashMap<String,String>? = null
    val names: HashMap<String,String>? = null
    val colors: HashMap<String,String>? = null

    fun convertToDiagramValues() : List<LineDiagramData> {
        val separatedAxises = columns!!.map { separateAxis(it) }

        var xAxis: AxisInfo? = null
        val yAxises = mutableListOf<AxisInfo>()
        for (separatedAxis in separatedAxises) {
            val axisInfo = AxisInfo(
                    separatedAxis,
                    Color.parseColor(colors!![separatedAxis.title]?: "#000000"),
                    names!![separatedAxis.title]?: "x",
                    types!![separatedAxis.title]!!
            )
            if (separatedAxis.title == "x") {
                xAxis = axisInfo

            } else {
                yAxises.add(axisInfo)
            }
        }


        return yAxises.map {
            val values = mutableListOf<DiagramValue>()

            xAxis!!.separatedAxis.values.forEachIndexed { index, x ->
                values.add(DiagramValue(x, it.separatedAxis.values[index]))
            }

            LineDiagramData(values, it.name, it.color)
        }
    }


    private fun separateAxis(values: List<String>): SeparatedAxis =
            SeparatedAxis(values.first(), values.subList(1, values.size).map { it.toFloat() })

    private data class SeparatedAxis(val title: String, val values: List<Float>)
    private data class AxisInfo(val separatedAxis: SeparatedAxis, val color: Int, val name: String, val type: String)
}