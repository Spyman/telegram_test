package com.spyman.telegramconcurs.diagram.diagram_data

import android.view.MotionEvent
import android.view.VelocityTracker

open class PositionController() {
    protected var slowdownMultipler = 0.92f
    protected val minimumSpeed = 0.05f

    var position: Float = 0f
    protected var velocityTracker: VelocityTracker? = null

    protected var lastX: Float = 0f
    protected var lastY: Float = 0f

    protected var time: Long = 0

    protected var speed = 0f

    protected var startPosition = 0f
    protected var endPosition = 0f

    var touch = false

    fun onMotionEvent(motionEvent: MotionEvent) {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = motionEvent.x
                lastY = motionEvent.y

                velocityTracker?.clear()
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                velocityTracker?.addMovement(motionEvent)
                touch = true
            }

            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.apply {
                    addMovement(motionEvent)
                }

                position -= lastX - motionEvent.x
                lastX = motionEvent.x
            }

            MotionEvent.ACTION_UP -> {
                velocityTracker?.computeCurrentVelocity(14)
                speed = velocityTracker?.xVelocity?:0f
                velocityTracker?.recycle()
                velocityTracker = null
                startScrolling()
                touch = false
            }
        }

        checkBorders()
    }

    fun updateScroll(): Boolean {
        if (touch) { return false }
        if (Math.abs(speed) < minimumSpeed) {
            return false
        }
        speed *= slowdownMultipler
        position += speed
        checkBorders()
        return true
    }

    fun startScrolling() {
        time = System.nanoTime()
    }

    fun updateBorders(startPosition: Float, endPosition: Float) {
        this.startPosition = startPosition
        this.endPosition = endPosition
        checkBorders()
    }

    protected fun checkBorders() {
        if (position > startPosition) {
            position = startPosition
        }
        if (position < -endPosition) {
            position = -endPosition
        }
    }
}