package uk.co.sentinelweb.cuer.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

//import java.time.Instant

@Serializable
data class SearchLocalDomain(
    var text: String = "",
    var isWatched: Boolean = true,
    var isNew: Boolean = true,
    var isLive: Boolean = false,
    val playlists: MutableSet<PlaylistDomain> = mutableSetOf(),
    var dateRangeType: DateRange = DateRange.PUBLISHED,
    @Contextual var fromDate: Instant? = null,
    @Contextual var toDate: Instant? = null
) : Domain {
    enum class DateRange { PUBLISHED, ADDED, WATCHED }
}