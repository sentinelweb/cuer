package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class SearchRemoteDomain(
    var text: String = "",
    var platform: PlatformDomain = PlatformDomain.YOUTUBE,
    var relatedToPlatformId: String? = null,
    var channelPlatformId: String? = null,
    var isLive: Boolean? = null,
    @Contextual var fromDate: Instant? = null,
    @Contextual var toDate: Instant? = null
)