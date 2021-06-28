package uk.co.sentinelweb.cuer.app.ui.ytplayer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import org.koin.core.context.GlobalContext.get
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class InterceptorFrameLayout constructor(c: Context, a: AttributeSet?, def: Int = 0) :
    FrameLayout(c, a, def) {

    constructor(c: Context, a: AttributeSet?) : this(c, a, 0)

    var listener: OnTouchInterceptListener? = null
    private val log: LogWrapper = get().get()

    init {
        log.tag(this)
    }

    interface OnTouchInterceptListener {
        fun touched()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            listener?.touched()
            log.d("onInterceptTouchEvent.ACTION_DOWN -  touched")
        }
        return false
    }
}