package uk.co.sentinelweb.cuer.core.wrapper

class SystemLogWrapper : LogWrapper {

    override var tag = APP_TAG

    override fun tag(obj: Any) {
        tag = obj::class.java.simpleName
    }

    override fun d(msg: String) = System.err.println("$tag: $msg")

    override fun e(msg: String, t: Throwable?) {
        System.err.println("$tag: $msg")
        t?.printStackTrace(System.err)
    }

    companion object {
        val APP_TAG = "Cuer"
    }
}