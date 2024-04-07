package uk.co.sentinelweb.cuer.core.wrapper

interface LogWrapper {
    var tag: String
    fun tag(obj: Any)
    fun d(msg: String)
    fun i(msg: String)
    fun w(msg: String)
    fun e(msg: String, t: Throwable? = null)
}

expect class SystemLogWrapper(tagObj: Any? = null) : LogWrapper

inline val <reified T> T.TAG: String
    @Suppress("unused")
    get() = T::class.simpleName ?: T::class.toString()