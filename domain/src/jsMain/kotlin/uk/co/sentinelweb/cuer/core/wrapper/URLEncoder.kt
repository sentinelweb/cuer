package uk.co.sentinelweb.cuer.core.wrapper

// fixme this my not work
external fun encodeURIComponent(str: String): String
external fun decodeURIComponent(str: String): String

actual object URLEncoder {
    actual fun encode(value: String, encoding: String): String =
        encodeURIComponent(value)

    actual fun decode(value: String, encoding: String): String =
        decodeURIComponent(value)

}
