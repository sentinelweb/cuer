package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

data class PlaylistEditState constructor(
    var isCreate: Boolean = false,
    var model: PlaylistEditModel? = null
) {
    lateinit var source: OrchestratorContract.Source
    lateinit var playlist: PlaylistDomain
}