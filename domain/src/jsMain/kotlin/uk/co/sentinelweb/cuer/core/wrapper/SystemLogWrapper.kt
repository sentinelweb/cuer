package uk.co.sentinelweb.cuer.core.wrapper

actual class SystemLogWrapper actual constructor(tagObj: Any?) : LogWrapper {

    init {
        tagObj?.also { tag(it) }
    }
    
    override var tag: String = "CuerJs"
        get() = field
        set(value) {
            field = value
        }

    override fun tag(obj: Any) {
        tag = obj::class.simpleName ?: obj.toString()
    }

    override fun d(msg: String) {
        console.log(msg)
    }

    override fun i(msg: String) {
        console.log(msg)
    }

    override fun w(msg: String) {
        console.log(msg)
    }

    override fun e(msg: String, t: Throwable?) {
        console.log(msg, t)
    }
}