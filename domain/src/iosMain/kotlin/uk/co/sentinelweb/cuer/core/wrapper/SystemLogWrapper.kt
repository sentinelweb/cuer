package uk.co.sentinelweb.cuer.core.wrapper

actual class SystemLogWrapper actual constructor(tagObj: Any?) : LogWrapper {

    init {
        tagObj?.also { tag(it) }
    }

    override var tag: String = "CuerIos"
        get() = field
        set(value) {
            field = value
        }

    override fun tag(obj: Any) {
        tag = obj::class.simpleName ?: obj.toString()
    }

    override fun d(msg: String) {
        println("DEBUG: $tag: $msg")
    }

    override fun i(msg: String) {
        println("INFO: $tag: $msg")
    }

    override fun w(msg: String) {
        println("WARN: $tag: $msg")
    }

    override fun e(msg: String, t: Throwable?) {
        println("ERROR: $tag: $msg - ${t?.message ?: "No Exception"}")
        t?.printStackTrace()
    }
}