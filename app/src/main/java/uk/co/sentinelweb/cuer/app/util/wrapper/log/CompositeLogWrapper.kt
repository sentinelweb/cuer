package uk.co.sentinelweb.cuer.app.util.wrapper.log

import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain

class CompositeLogWrapper constructor(
    private val firebase: FirebaseWrapper,
    private val android: AndroidLogWrapper,
    private val buildConfig: BuildConfigDomain
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
        if (buildConfig.isDebug) {
            firebase.setCrashlyticTag(tag)
            firebase.logMessage("DEBUG: $tag: $msg")
        }
    }

    override fun i(msg: String) {
        android.i(msg)
        firebase.setCrashlyticTag(tag)
        firebase.logMessage("INFO: $tag: $msg")
    }

    override fun w(msg: String) {
        android.w(msg)
        firebase.setCrashlyticTag(tag)
        firebase.logMessage("WARN: $tag: $msg")
    }

    override fun e(msg: String, t: Throwable?) {
        android.e(msg, t)
        firebase.setCrashlyticTag(tag)
        firebase.logMessage("ERROR: $tag: $msg")
        t?.apply { firebase.logException(t) }
    }

}