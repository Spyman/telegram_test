package com.spyman.telegramconcurs.diagram

class DefaultValueFormatter: ValueFormatter {
    override fun granularity(countOnScreen: Float) = 1f

    override fun format(value: Float): String =
        value.toString()

}