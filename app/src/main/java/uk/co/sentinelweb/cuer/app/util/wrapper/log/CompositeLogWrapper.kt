package uk.co.sentinelweb.cuer.app.util.wrapper.log

import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class CompositeLogWrapper constructor(
    private val firebase: FirebaseWrapper,
    private val android: AndroidLogWrapper
) : LogWrapper {
    override var tag: String
        get() = android.tag
        set(value) {
            android.tag = value
        }

    override fun tag(obj: Any) {
        tag = obj::class.java.simpleName
    }

    override fun d(msg: String) {
        android.d(msg)
    }

    override fun i(msg: String) {
        android.i(msg)
    }

    override fun w(msg: String) {
        android.w(msg)
    }

    override fun e(msg: String, t: Throwable?) {
        android.e(msg, t)
        t?.apply {
            firebase.setCrashlyticTag(tag)
            firebase.logMessage(msg)
            firebase.logException(t)
        }
    }

}