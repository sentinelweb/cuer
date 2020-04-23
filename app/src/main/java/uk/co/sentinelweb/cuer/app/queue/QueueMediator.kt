package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class QueueMediator constructor(
    private val state: QueueMediatorState,
    private val repository: MediaDatabaseRepository,
    private val mediaMapper: MediaToPlaylistItemMapper,
    private val contextProvider: CoroutineContextProvider
) : QueueMediatorContract.Mediator {

    private val consumerListeners: MutableList<QueueMediatorContract.ConsumerListener> =
        mutableListOf()
    private val producerListeners: MutableList<QueueMediatorContract.ProducerListener> =
        mutableListOf()

    init {
        refreshQueue()
    }

    override fun onItemSelected(playlistItem: PlaylistItemDomain) {
        // todo if item not in queue? add it? or exception .. not sure
        state.queuePosition = state.currentPlayList
            ?.items
            ?.indexOfFirst { it.media.url == playlistItem.media.url }
            ?: 0
        updateCurrentItem()
    }

    override fun updateMediaItem(domain: MediaDomain) {
        // update media and playlist
    }

    override fun addConsumerListener(l: QueueMediatorContract.ConsumerListener) {
        consumerListeners.add(l)
    }

    override fun removeConsumerListener(l: QueueMediatorContract.ConsumerListener) {
        consumerListeners.remove(l)
    }

    override fun addProducerListener(l: QueueMediatorContract.ProducerListener) {
        producerListeners.add(l)
    }

    override fun removeProducerListener(l: QueueMediatorContract.ProducerListener) {
        producerListeners.remove(l)
    }

    override fun destroy() {
        // might not be needed if singleton
    }

    override fun nextItem() {
        state.queuePosition++
        // todo if loop, shuffle, etc
        if (state.queuePosition == state.currentPlayList!!.items.size) {
            state.queuePosition = 0
        }
        updateCurrentItem()
    }

    override fun lastItem() {
        state.queuePosition--
        // todo if loop, shuffle, etc
        if (state.queuePosition < 0) {
            state.queuePosition = state.currentPlayList!!.items.size - 1
        }
        updateCurrentItem()
    }

    private fun updateCurrentItem() {
        state.currentPlaylistItem = state.currentPlayList!!.items[state.queuePosition]
        consumerListeners.forEach { it.onItemChanged() }
    }

    override fun getCurrentItem(): PlaylistItemDomain? = state.currentPlaylistItem

    override fun removeItem(playlistItemDomain: PlaylistItemDomain) {
        state.jobs.add(contextProvider.MainScope.launch {
            repository.delete(playlistItemDomain.media)
            refreshQueue()
        })
    }

    override fun refreshQueue() {
        state.jobs.add(contextProvider.MainScope.launch {
            // todo preserve position by checking current item
            repository
                .loadList(null)
                .also { state.mediaList = it }
                .map { mediaMapper.map(it) }
                .let { PlaylistDomain(items = it) }
                .also { state.currentPlayList = it }
                .also { playlist ->
                    producerListeners.forEach { l -> l.onPlaylistUpdated(playlist) }
                }
        })
    }

    override fun getPlayList(): PlaylistDomain? = state.currentPlayList
}