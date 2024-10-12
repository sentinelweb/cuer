package uk.co.sentinelweb.cuer.core.wrapper

expect object URLEncoder {
    fun encode(value: String, encoding: String = "UTF-8"): String
    fun decode(value: String, encoding: String = "UTF-8"): String
}
