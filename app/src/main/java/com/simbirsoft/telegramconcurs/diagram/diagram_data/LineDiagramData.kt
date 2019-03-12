package com.simbirsoft.telegramconcurs.diagram.diagram_data

data class LineDiagramData(val values: List<DiagramValue>) {
    private var _minimumValue: Float? = null
    var minimumValue: Float
        get() = _minimumValue.let {
            it ?: (values.minBy { it.y }?.y?:0f)
        }
        set(value) { _minimumValue = value}

    private var _maximumValue: Float? = null
    var maximumValue: Float
        get() = _maximumValue.let {
            it ?: (values.maxBy { it.y }?.y ?: 0f)
        }
        set(value) { _maximumValue = value}
}
