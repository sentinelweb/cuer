package uk.co.sentinelweb.cuer.core.wrapper

actual class SystemLogWrapper actual constructor(tagObj: Any?) : LogWrapper {

    override var tag = APP_TAG

    override fun tag(obj: Any) {
        tag = obj::class.java.simpleName
    }

    override fun d(msg: String) = System.out.println("$tag:d: $msg")

    override fun i(msg: String) = System.out.println("$tag:i: $msg")

    override fun w(msg: String) = System.err.println("$tag:w: $msg")

    override fun e(msg: String, t: Throwable?) {
        System.err.println("$tag:e: $msg")
        t?.printStackTrace(System.err)
    }

    companion object {
        val APP_TAG = "Cuer"
    }
}