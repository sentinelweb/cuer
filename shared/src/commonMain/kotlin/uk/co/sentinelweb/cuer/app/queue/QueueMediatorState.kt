package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.Job
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

data class QueueMediatorState constructor(
    var playlistIdentifier: Identifier<*> = NO_PLAYLIST,
    var playlist: PlaylistDomain? = null,
    var currentItem: PlaylistItemDomain? = null,
    val jobs: MutableList<Job> = mutableListOf()
)