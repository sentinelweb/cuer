package uk.co.sentinelweb.cuer.core.wrapper


actual object URLEncoder {
    actual fun encode(value: String, encoding: String): String {
        return java.net.URLEncoder.encode(value, encoding)
    }

    actual fun decode(value: String, encoding: String): String {
        return java.net.URLDecoder.decode(value, encoding)
    }
}
