package uk.co.sentinelweb.cuer.domain.creator

import uk.co.sentinelweb.cuer.domain.GUID

actual class GUIDCreator actual constructor() {
    // https://stackoverflow.com/questions/105034/how-do-i-create-a-guid-uuid/2117523#2117523
    actual fun create(): GUID = TODO("find a way to generate a UUID on JS")
}
