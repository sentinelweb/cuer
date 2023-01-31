package uk.co.sentinelweb.cuer.domain.creator

import platform.Foundation.NSUUID

actual class GUIDCreator {
    actual fun create(): String = NSUUID().UUIDString
}
