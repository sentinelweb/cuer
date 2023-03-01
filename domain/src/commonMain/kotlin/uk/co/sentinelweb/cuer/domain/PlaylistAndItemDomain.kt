package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract

@Serializable
data class PlaylistAndItemDomain(
    val item: PlaylistItemDomain,
    val playlistId: OrchestratorContract.Identifier<GUID>? = item.playlistId,
    val playlistTitle: String? = null,
) : Domain