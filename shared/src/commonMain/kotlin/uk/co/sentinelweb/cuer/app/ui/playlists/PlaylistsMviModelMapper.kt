package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.View

class PlaylistsMviModelMapper {

    fun map(state: MviStore.State): View.Model = View.Model(
        currentPlaylistId = (-1L).toIdentifier(OrchestratorContract.Source.LOCAL),
        items = listOf(),
        title = "Playlists"
    )
}