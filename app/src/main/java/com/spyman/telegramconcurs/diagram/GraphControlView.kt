package com.spyman.telegramconcurs.diagram

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
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
    var gestureType: TouchStartPosition? = null

    val touchZoneSize = 25 // todo change to dimention

    protected var gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            graph?.apply {
                abortScroll()
            }
            gestureType = e?.x?.let {x ->
                return@let window?.let {
                    if (x < it.x || x > it.x + it.width) {
                        return@let TouchStartPosition.OUTSIDE
                    }
                    if (x - it.x < touchZoneSize) {
                        return@let TouchStartPosition.LEFT
                    }
                    if (x - it.x > it.width - touchZoneSize) {
                        return@let TouchStartPosition.RIGHT
                    }
                    return@let TouchStartPosition.MIDDLE
                }
            }
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return gestureType?.let {
                return when (it) {
                    TouchStartPosition.MIDDLE -> {
                        moveWindow(distanceX)
                        return true
                    }
                    TouchStartPosition.RIGHT -> {
                        scaleToRight(distanceX)
                        return true
                    }

                    else -> return false
                }
            }?:false
        }
    })

    fun attachToGraph(graph: DiagramView) {
        removeAllViews()
        this.graph = graph

        diagramView = DiagramView(context)
        diagramView.setData(graph.getData())
        diagramView.isXAxisVisible = false
        diagramView.isYAxisVisible = false
        diagramView.isEnabled = false
        graph.onPositionChangeListener = object:OnValueChangeListener<Float> {
            override fun onChange(newValue: Float) {
                window?.x = -graph.position/graph.getXScale()*measuredWidth.toFloat()/graph.xSize.toFloat()
            }
        }

        addView(diagramView)
        window = ImageView(context)
        window?.post {
            window?.setImageResource(R.drawable.window)
            val layoutParams = RelativeLayout.LayoutParams(Math.round(measuredWidth.toFloat()/graph.getXScale()*measuredWidth.toFloat()/graph.xSize.toFloat()), ViewGroup.LayoutParams.MATCH_PARENT)

            window?.layoutParams = layoutParams
            window?.x = -graph.position/graph.getXScale()
            window?.requestLayout()
        }
        addView(window)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        window?.measure(widthMeasureSpec, heightMeasureSpec)
    }

    protected fun moveWindow(distanceX: Float) {
        window?.let {window ->
            graph?.let {
                it.updatePosition(it.position + distanceX * it.getXScale() * it.xSize.toFloat()/measuredWidth.toFloat())
            }
        }
    }

    protected fun scaleToRight(distanceX: Float) {
        window?.let { window ->
            graph?.let {
                val layoutParams = window.layoutParams
                layoutParams.width -= Math.round(distanceX)
                it.setXScale(measuredWidth/layoutParams.width.toFloat())
                window.requestLayout()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        performClick()
        return gestureDetector.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    enum class TouchStartPosition {
        LEFT, RIGHT, MIDDLE, OUTSIDE
    }
}