package uk.co.sentinelweb.cuer.core.ext

import platform.Foundation.NSString
import platform.Foundation.create

fun String.nsString(): NSString {
    return NSString.create(string = this)
}
