package uk.co.sentinelweb.cuer.core.wrapper

import platform.Foundation.*
import uk.co.sentinelweb.cuer.core.ext.nsString

actual object URLEncoder {
    actual fun encode(value: String, encoding: String): String {
        val allowedCharacters = NSCharacterSet.URLQueryAllowedCharacterSet
        return value.nsString().stringByAddingPercentEncodingWithAllowedCharacters(allowedCharacters) ?: value
    }

    actual fun decode(value: String, encoding: String): String {
        return value.nsString().stringByRemovingPercentEncoding() ?: value
    }
}
