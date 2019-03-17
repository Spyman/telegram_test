package com.spyman.telegramconcurs.diagram

interface OnValueChangeListener<T> {
    fun onChange(newValue: T)
}