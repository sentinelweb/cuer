package uk.co.sentinelweb.cuer.app.util.wrapper

import android.util.Log
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class AndroidLogWrapper : LogWrapper {

    override var tag = APP_TAG

    override fun tag(obj: Any) {
        tag = obj::class.java.simpleName
    }

    override fun d(msg: String) = Log.d(tag, msg).let { Unit }

    override fun e(msg: String, t: Throwable?) = Log.e(tag, msg, t).let { Unit }

    companion object {
        val APP_TAG = "Cuer"
    }
}