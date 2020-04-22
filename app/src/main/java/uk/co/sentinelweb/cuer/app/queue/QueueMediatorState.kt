package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.Job
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

data class QueueMediatorState constructor(
    var currentPlayList :PlaylistDomain? = null,
    var currentPlaylistItem:PlaylistItemDomain? = null,
    var mediaList: List<MediaDomain> = listOf(),
    var queuePosition: Int = 0,
    val jobs: MutableList<Job> = mutableListOf()
)