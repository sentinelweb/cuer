package uk.co.sentinelweb.cuer.domain.creator

import uk.co.sentinelweb.cuer.domain.GUID
import java.util.*

actual class GuidCreator actual constructor() {
    actual fun create(): GUID = GUID(UUID.randomUUID().toString())
}
