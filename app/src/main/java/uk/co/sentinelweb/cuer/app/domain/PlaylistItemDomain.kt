package uk.co.sentinelweb.cuer.app.domain

import java.time.Instant

data class PlaylistItemDomain(
    val media: MediaDomain,
    val dateAdded: Instant
)
