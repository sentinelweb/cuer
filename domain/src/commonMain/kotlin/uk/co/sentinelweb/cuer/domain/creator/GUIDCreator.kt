package uk.co.sentinelweb.cuer.domain.creator

import uk.co.sentinelweb.cuer.domain.GUID

expect class GUIDCreator() {
    fun create(): GUID
}
