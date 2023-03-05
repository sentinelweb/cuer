package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class MultiTouchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), KoinComponent {

    private val startPoint = PointF()
    private var dragPoint: PointF? = null
    private var lastDistance = 0f
    private var currentScale = 1f
    private val log: LogWrapper by inject()

    var callbacks: Callbacks? = null

    interface Callbacks {
        fun onDown()
        fun onResize(currentScale: Float)
        fun onMove(dx: Float, dy: Float)
        fun onClick()
        fun onCommit()
    }

    init {
        log.tag(this)
        setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    if (event.pointerCount == 1) {
                        startPoint.set(event.x, event.y)
                        log.d("ACTION_DOWN: $startPoint")
//                    dragPoint.set(event.x, event.y)
                        callbacks?.onDown()
                    }
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    lastDistance = getDistance(event)
                }

                MotionEvent.ACTION_MOVE -> {
                    log.d("ACTION_MOVE: ${event.pointerCount} ${event.x} ${event.y} ${event.pointerCount}}")
                    when {
                        event.pointerCount == 1 -> {
                            val dx = event.x - startPoint.x
                            val dy = event.y - startPoint.y
                            log.d("move: $dx, $dy")
                            dragPoint = PointF(event.x, event.y)
                            callbacks?.onMove(dx, dy)
//                            translationX += dx
//                            translationY += dy
                        }

                        event.pointerCount == 2 -> {
                            val distance = getDistance(event)
                            val scale = distance / lastDistance
                            lastDistance = distance
                            val newScale = currentScale * scale
                            log.d("scale: $currentScale")
                            if (newScale > 0.5f && newScale < 5f) {
                                currentScale = newScale
                                callbacks?.onResize(currentScale)
//                                scaleX = currentScale
//                                scaleY = currentScale
                            }
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (event.pointerCount == 1) {
                        if (dragPoint == null && currentScale == 1f) {
                            performClick()
                        } else {
                            callbacks?.onCommit()
                        }
                        dragPoint = null
                        currentScale = 1f
                    }
                }
            }
            true
        }
    }

    private fun getDistance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    override fun performClick(): Boolean {
        super.performClick()
        callbacks?.onClick()
        log.d("click: ")
        return true
    }
}
