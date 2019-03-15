package com.spyman.telegramconcurs.diagram

interface ValueFormatter {
    fun format(value: Float): String
    fun granularity(countOnScreen: Float): Float
}