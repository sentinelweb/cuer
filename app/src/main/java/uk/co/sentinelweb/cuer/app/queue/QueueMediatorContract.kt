package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface QueueMediatorContract {

    interface Shared {
        val currentItem: PlaylistItemDomain?
        val currentItemIndex: Int?
        val playlist: PlaylistDomain?
        val playlistId: Long?
        val currentItemFlow: Flow<PlaylistItemDomain?>
    }

    interface Producer : Shared {
        fun onItemSelected(playlistItem: PlaylistItemDomain, forcePlay: Boolean = false, resetPosition: Boolean = false)
        fun destroy()
        fun refreshQueueBackground()
        suspend fun refreshQueue()
        fun playNow()
        suspend fun playNow(playlist: PlaylistDomain, playlistItemId: Long?)
        suspend fun playNow(playlistId: Long, playlistItemId: Long?)
        fun deleteItem(index: Int)
        suspend fun switchToPlaylist(id: Long)
    }

    interface Consumer : Shared {
        val currentPlaylistFlow: Flow<PlaylistDomain>
        fun onTrackEnded(media: MediaDomain?)
        fun nextItem()
        fun previousItem()
        fun updateMediaItem(updatedMedia: MediaDomain)
    }

}