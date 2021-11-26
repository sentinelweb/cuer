package uk.co.sentinelweb.cuer.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

//import java.time.Instant

@Serializable
data class PlaylistItemDomain(
    val id: Long? = null,
    val media: MediaDomain,
    @Contextual val dateAdded: Instant,
    val order: Long,
    val archived: Boolean = false,
    val playlistId: Long? = null
) : Domain
