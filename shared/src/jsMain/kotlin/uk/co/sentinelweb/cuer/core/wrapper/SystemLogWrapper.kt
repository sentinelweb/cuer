package uk.co.sentinelweb.cuer.core.wrapper

actual class SystemLogWrapper : LogWrapper {
    override var tag: String
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun tag(obj: Any) {
        TODO("Not yet implemented")
    }

    override fun d(msg: String) {
        TODO("Not yet implemented")
    }

    override fun i(msg: String) {
        TODO("Not yet implemented")
    }

    override fun w(msg: String) {
        TODO("Not yet implemented")
    }

    override fun e(msg: String, t: Throwable?) {
        TODO("Not yet implemented")
    }
}