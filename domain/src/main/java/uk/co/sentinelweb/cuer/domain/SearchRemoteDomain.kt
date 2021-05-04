package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class SearchRemoteDomain(
    var text: String? = null,
    var platform: PlatformDomain = PlatformDomain.YOUTUBE,
    var relatedToMediaPlatformId: String? = null,
    var relatedToMediaTitle: String? = null,
    var channelPlatformId: String? = null,
    var isLive: Boolean = false,
    @Contextual var fromDate: LocalDateTime? = null,
    @Contextual var toDate: LocalDateTime? = null,
    val lang: String = "en",
    val order: Order = Order.RATING
) {
    enum class Order { RELEVANCE, RATING, VIEWCOUNT, DATE, TITLE }
}