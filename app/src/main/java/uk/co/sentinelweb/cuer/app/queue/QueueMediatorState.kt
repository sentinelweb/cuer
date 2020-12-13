package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.Job
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

data class QueueMediatorState constructor(
    var playlistId: Long? = null,
    var playlist: PlaylistDomain? = null,
    var currentItem: PlaylistItemDomain? = null,
    val jobs: MutableList<Job> = mutableListOf()
)