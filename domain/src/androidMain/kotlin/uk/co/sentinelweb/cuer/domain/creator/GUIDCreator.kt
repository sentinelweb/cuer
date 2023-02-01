package uk.co.sentinelweb.cuer.domain.creator

import uk.co.sentinelweb.cuer.domain.GUID
import java.util.*

actual class GUIDCreator actual constructor() {
    actual fun create(): GUID = GUID(UUID.randomUUID().toString())
}
