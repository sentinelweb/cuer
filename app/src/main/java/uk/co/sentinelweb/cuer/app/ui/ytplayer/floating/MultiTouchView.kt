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
    private var lastDistance: PointF? = null
    private var currentScale: PointF? = null
    private var isDragging = false
    private val log: LogWrapper by inject()

    var callbacks: Callbacks? = null

    interface Callbacks {
        fun onDown()
        fun onResize(currentScale: PointF)
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
                        // log.d("ACTION_DOWN: $startPoint")
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
                            isDragging = isDragging || dx > 0 || dy > 0
                            // log.d("move: $dx, $dy")
                            if (currentScale == null) {
                                callbacks?.onMove(dx, dy)
                            }
                        }

                        event.pointerCount == 2 -> {
                            if (currentScale == null) {
                                currentScale = PointF(1f, 1f)
                                lastDistance = PointF(1f, 1f)
                            }
                            val distance = getDistance(event)
                            val scale = PointF(
                                distance.x / lastDistance!!.x,
                                distance.y / lastDistance!!.y
                            )
                            lastDistance = distance
                            val newScale = PointF(
                                currentScale!!.x * scale.x,
                                currentScale!!.y * scale.y,
                            )
                            if (newScale.x > 0.5f && newScale.x < 5f && newScale.y > 0.5f && newScale.y < 5f) {
                                currentScale = newScale
                                callbacks?.onResize(currentScale!!)
                            }
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (event.pointerCount == 1) {
                        if (!isDragging && currentScale == null) {
                            performClick()
                        } else {
                            callbacks?.onCommit()
                        }
                        isDragging = false
                        currentScale = null
                    }
                }
            }
            true
        }
    }

    private fun getDistance(event: MotionEvent): PointF {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return PointF(x, y)
        //return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    override fun performClick(): Boolean {
        super.performClick()
        callbacks?.onClick()
        log.d("click: ")
        return true
    }
}
