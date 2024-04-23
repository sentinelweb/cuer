package uk.co.sentinelweb.cuer.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier

@Serializable
data class PlaylistItemDomain(
    val id: Identifier<GUID>? = null,
    val media: MediaDomain,
    @Contextual val dateAdded: Instant,
    val order: Long,
    val archived: Boolean = false,
    val playlistId: Identifier<GUID>? // todo make this non nullable .. maybe(= NO_PLAYLIST)
) : Domain {

    companion object {
        const val FLAG_ARCHIVED = 1L
    }
}
