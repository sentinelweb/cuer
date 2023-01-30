package uk.co.sentinelweb.cuer.domain.creator

import java.util.*

actual class GUIDCreator {
    actual fun create(): String = UUID.randomUUID().toString()
}