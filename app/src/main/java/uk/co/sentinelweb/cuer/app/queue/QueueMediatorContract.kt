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

        //        fun addProducerListener(l: ProducerListener)
//        fun removeProducerListener(l: ProducerListener)
        fun destroy()
        fun refreshQueueBackground()//after: (() -> Unit)? = null
        //fun refreshQueueFrom(playlistDomain: PlaylistDomain)//after: (() -> Unit)? = null

        //fun itemRemoved(playlistItemDomain: PlaylistItemDomain)
        suspend fun refreshQueue()//after: (() -> Unit)? = null
        fun playNow()
        suspend fun playNow(playlist: PlaylistDomain, playlistItemId: Long?)
        suspend fun playNow(playlistId: Long, playlistItemId: Long?)
        fun deleteItem(index: Int)
        fun switchToPlaylist(id: Long)
        //fun refreshHeaderData()
    }

    interface Consumer : Shared {
        val currentPlaylistFlow: Flow<PlaylistDomain>
        fun onTrackEnded(media: MediaDomain?)
        fun nextItem()
        fun previousItem()

        //        fun addConsumerListener(l: ConsumerListener)
//        fun removeConsumerListener(l: ConsumerListener)
        fun updateMediaItem(updatedMedia: MediaDomain)
    }
//
//    interface ConsumerListener {
//        fun onItemChanged()
//        fun onPlaylistUpdated()
//    }

//    interface ProducerListener {
//        fun onPlaylistUpdated(list: PlaylistDomain)
//        fun onItemChanged()
//    }
}