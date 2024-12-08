package uk.co.sentinelweb.cuer.core.wrapper

import uk.co.sentinelweb.cuer.core.mappers.Format
import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProviderImpl

actual class SystemLogWrapper actual constructor(tagObj: Any?) : LogWrapper {

    private val timeProvider: TimeProvider = TimeProviderImpl()
    private val formatter: TimeFormatter = TimeFormatter()
    private val timeStamp: String
        get() {
            return formatter.format(timeProvider.currentTimeMillis(), Format.MILLIS)
        }

    override var tag = APP_TAG

    init {
        tagObj?.also { tag(it) }
    }

    override fun tag(obj: Any) {
        tag = obj::class.java.simpleName
    }

    override fun d(msg: String) = System.out.println("[$timeStamp] $tag:d: $msg")

    override fun i(msg: String) = System.out.println("[$timeStamp] $tag:i: $msg")

    override fun w(msg: String) = System.err.println("[$timeStamp] $tag:w: $msg")

    override fun e(msg: String, t: Throwable?) {
        System.err.println("[$timeStamp] $tag:e: $msg")
        t?.printStackTrace(System.err)
    }

    companion object {
        val APP_TAG = "Cuer"
    }
}
