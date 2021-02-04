package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface QueueMediatorContract {

    interface Shared {
        val currentItem: PlaylistItemDomain?
        val currentItemIndex: Int?
        val playlist: PlaylistDomain?
        val playlistId: OrchestratorContract.Identifier<*>?
        val currentItemFlow: Flow<PlaylistItemDomain?>
    }

    interface Producer : Shared {
        fun onItemSelected(playlistItem: PlaylistItemDomain, forcePlay: Boolean = false, resetPosition: Boolean = false)
        fun destroy()
        fun refreshQueueBackground()
        suspend fun refreshQueue()
        fun playNow()
        suspend fun playNow(identifier: OrchestratorContract.Identifier<*>, playlistItemId: Long?)
        //fun deleteItem(index: Int)
        suspend fun switchToPlaylist(identifier: OrchestratorContract.Identifier<*>)
    }

    interface Consumer : Shared {
        val currentPlaylistFlow: Flow<PlaylistDomain>
        fun onTrackEnded(media: MediaDomain?)
        fun nextItem()
        fun previousItem()
        fun updateMediaItem(updatedMedia: MediaDomain)
    }

}