package uk.co.sentinelweb.cuer.app.queue

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface QueueMediatorContract {


    interface Shared {
        val currentItem: PlaylistItemDomain?
        val currentItemIndex: Int?
        val playlist: PlaylistDomain?
    }

    interface Producer : Shared {
        fun onItemSelected(playlistItem: PlaylistItemDomain)
        fun addProducerListener(l: ProducerListener)
        fun removeProducerListener(l: ProducerListener)
        fun destroy()
        fun refreshQueueBackground()//after: (() -> Unit)? = null
        fun refreshQueueFrom(playlistDomain: PlaylistDomain)//after: (() -> Unit)? = null
        fun itemRemoved(playlistItemDomain: PlaylistItemDomain)
        suspend fun refreshQueue()//after: (() -> Unit)? = null
        fun playNow()
    }

    interface Consumer : Shared {
        fun onTrackEnded(media: MediaDomain?)
        fun nextItem()
        fun previousItem()
        fun addConsumerListener(l: ConsumerListener)
        fun removeConsumerListener(l: ConsumerListener)
        fun updateMediaItem(updatedMedia: MediaDomain)
    }

    interface ConsumerListener {
        fun onItemChanged()
    }

    interface ProducerListener {
        fun onPlaylistUpdated(list: PlaylistDomain)
        fun onItemChanged()
    }
}