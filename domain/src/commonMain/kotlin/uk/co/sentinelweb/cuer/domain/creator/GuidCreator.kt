package uk.co.sentinelweb.cuer.domain.creator

import uk.co.sentinelweb.cuer.domain.GUID

expect class GuidCreator() {
    fun create(): GUID
}
