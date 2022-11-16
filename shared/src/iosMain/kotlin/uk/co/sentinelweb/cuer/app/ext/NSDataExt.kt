package uk.co.sentinelweb.cuer.app.ext

import platform.Foundation.*

@Suppress("CAST_NEVER_SUCCEEDS")
fun String.nsdata(): NSData? {
    return (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)
}

fun NSData.string(): String? {
    return NSString.create(this, NSUTF8StringEncoding) as String?
}