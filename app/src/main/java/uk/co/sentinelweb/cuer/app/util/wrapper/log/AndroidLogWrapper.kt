package uk.co.sentinelweb.cuer.app.util.wrapper.log

import android.util.Log
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain

class AndroidLogWrapper(
    private val buildConfig: BuildConfigDomain
) : LogWrapper {

    override var tag = APP_TAG

    override fun tag(obj: Any) {
        tag = obj::class.java.simpleName
    }

    override fun d(msg: String) {
        if (buildConfig.isDebug) Log.d(tag, msg)
    }

    override fun i(msg: String) {
        Log.i(tag, msg)
    }

    override fun w(msg: String) {
        Log.w(tag, msg)
    }

    override fun e(msg: String, t: Throwable?) {
        Log.e(tag, msg, t)
    }

    companion object {
        val APP_TAG = "Cuer"
    }
}