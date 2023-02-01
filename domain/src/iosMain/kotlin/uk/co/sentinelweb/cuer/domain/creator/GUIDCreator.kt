package uk.co.sentinelweb.cuer.domain.creator

import platform.Foundation.NSUUID
import uk.co.sentinelweb.cuer.domain.GUID

actual class GUIDCreator actual constructor() {
    actual fun create(): GUID = GUID(NSUUID().UUIDString)
}
