package uk.co.sentinelweb.cuer.domain

import java.time.Instant

data class PlaylistItemDomain(
    val media: MediaDomain,
    val dateAdded: Instant,
    val order: Long
)
