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
        fun lastItem()
        fun getCurrentItem(): PlaylistItemDomain?
        fun refreshQueue()
        fun getPlayList(): PlaylistDomain?
        fun addProducerListener(l: ProducerListener)
        fun removeProducerListener(l: ProducerListener)
        fun removeItem(playlistItemDomain: PlaylistItemDomain)
        fun onTrackEnded(media: MediaDomain?)
    }

    interface ConsumerListener {
        fun onItemChanged()
    }

    interface ProducerListener {
        fun onPlaylistUpdated(list:PlaylistDomain)
    }
}