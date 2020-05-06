package uk.co.sentinelweb.cuer.domain

import java.time.Instant

data class PlaylistItemDomain(
    val id: String? = null,
    val media: MediaDomain,
    val dateAdded: Instant,
    val order: Long
)
