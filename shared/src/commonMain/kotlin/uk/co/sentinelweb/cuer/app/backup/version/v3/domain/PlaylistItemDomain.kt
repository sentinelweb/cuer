package uk.co.sentinelweb.cuer.app.backup.version.v3.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.Domain

//import java.time.Instant

@Serializable
data class PlaylistItemDomain(
    val id: Long? = null,
    val media: MediaDomain,
    @Contextual val dateAdded: Instant,
    val order: Long,
    val archived: Boolean = false,
    val playlistId: Long? = null
) : Domain {
//    companion object {
//        const val FLAG_ARCHIVED = 1L
//    }
}
