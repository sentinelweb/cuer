package uk.co.sentinelweb.cuer.app.queue

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface QueueMediatorContract {

    interface Mediator {
        fun onItemSelected(playlistItem: PlaylistItemDomain)
        fun updateMediaItem(media: MediaDomain)
        fun addConsumerListener(l: ConsumerListener)
        fun removeConsumerListener(l: ConsumerListener)
        fun destroy()
        fun nextItem()
        fun previousItem()
        fun getCurrentItem(): PlaylistItemDomain?
        fun getPlaylist(): PlaylistDomain?
        fun addProducerListener(l: ProducerListener)
        fun removeProducerListener(l: ProducerListener)
        suspend fun removeItem(playlistItemDomain: PlaylistItemDomain)
        fun onTrackEnded(media: MediaDomain?)
        fun moveItem(fromPosition: Int, toPosition: Int)
        fun getItemFor(url: String): PlaylistItemDomain?
        fun itemIndex(item: PlaylistItemDomain): Int?
        fun refreshQueueBackground(after: (() -> Unit)? = null)
        suspend fun refreshQueue(after: (() -> Unit)? = null)
    }

    interface ConsumerListener {
        fun onItemChanged()
    }

    interface ProducerListener {
        fun onPlaylistUpdated(list:PlaylistDomain)
    }
}