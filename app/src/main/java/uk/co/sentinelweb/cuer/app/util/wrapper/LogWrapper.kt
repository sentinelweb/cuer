package uk.co.sentinelweb.cuer.app.util.wrapper

import android.util.Log

class LogWrapper {

    var tag = APP_TAG

    fun tag(obj: Any) {
        tag = obj::class.java.simpleName
    }

    fun d(msg:String) = Log.d(tag, msg)

    fun e(msg:String, t:Throwable? = null) = Log.e(tag, msg, t)

    companion object {
        val APP_TAG = "Cuer"
    }
}