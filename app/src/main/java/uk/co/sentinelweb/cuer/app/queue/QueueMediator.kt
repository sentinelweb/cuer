package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistDomainMode.LOOP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistDomainMode.SINGLE
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class QueueMediator constructor(
    private val state: QueueMediatorState,
    private val repository: MediaDatabaseRepository,
    private val mediaMapper: MediaToPlaylistItemMapper,
    private val contextProvider: CoroutineContextProvider,
    private val mediaSessionManager: MediaSessionManager
) : QueueMediatorContract.Mediator {

    private val consumerListeners: MutableList<QueueMediatorContract.ConsumerListener> =
        mutableListOf()
    private val producerListeners: MutableList<QueueMediatorContract.ProducerListener> =
        mutableListOf()

    init {
        refreshQueue()
    }

    override fun onItemSelected(playlistItem: PlaylistItemDomain) {
        if (playlistItem != state.currentPlaylistItem) {
            state.queuePosition = state.currentPlayList
                ?.items
                ?.indexOfFirst { it.media.url == playlistItem.media.url }
                ?: throw IllegalStateException("playlistItem not in playlist")
            updateCurrentItem()
        }
    }

    override fun updateMediaItem(media: MediaDomain) {
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
        // save queue position
    }

    override fun nextItem() {
        if (state.currentPlayList?.mode == LOOP || state.currentPlayList?.mode == SINGLE) {
            state.queuePosition++
            if (state.currentPlayList?.mode == LOOP &&
                state.queuePosition >= state.currentPlayList!!.items.size
            ) {
                state.queuePosition = 0
            }
        }
        if (state.queuePosition < state.currentPlayList!!.items.size) {
            updateCurrentItem()
        }
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
        state.currentPlaylistItem?.apply {
            mediaSessionManager.setMedia(media)
        }
        consumerListeners.forEach { it.onItemChanged() }
    }

    override fun getPlayList(): PlaylistDomain? = state.currentPlayList

    override fun getCurrentItem(): PlaylistItemDomain? = state.currentPlaylistItem

    override fun removeItem(playlistItemDomain: PlaylistItemDomain) {
        state.jobs.add(contextProvider.MainScope.launch {
            repository.delete(playlistItemDomain.media)
            refreshQueue()
        })
    }

    override fun onTrackEnded(media: MediaDomain?) {
        nextItem()
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
}