package uk.co.sentinelweb.cuer.domain

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract

data class PlaylistStatDomain constructor(
    val playlistId: OrchestratorContract.Identifier<GUID>,
    val itemCount: Int,
    val watchedItemCount: Int
) : Domain