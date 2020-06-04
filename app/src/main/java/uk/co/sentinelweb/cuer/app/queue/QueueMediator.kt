package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.util.helper.PlaylistMutator
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class QueueMediator constructor(
    private val state: QueueMediatorState,
    private val repository: MediaDatabaseRepository,
    private val mediaMapper: MediaToPlaylistItemMapper,
    private val contextProvider: CoroutineContextProvider,
    private val mediaSessionManager: MediaSessionManager,
    private val playlistMutator: PlaylistMutator
) : QueueMediatorContract.Mediator {

    private val consumerListeners: MutableList<QueueMediatorContract.ConsumerListener> =
        mutableListOf()
    private val producerListeners: MutableList<QueueMediatorContract.ProducerListener> =
        mutableListOf()

    init {
        refreshQueueBackground()
    }

    override fun onItemSelected(playlistItem: PlaylistItemDomain) {
        state.currentPlaylist
            ?.takeIf { playlistItem != state.currentPlaylistItem }
            ?.let {
                state.currentPlaylist = playlistMutator.playItem(it, playlistItem)
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
        state.currentPlaylist?.let { currentPlaylist ->
            state.currentPlaylist = playlistMutator.gotoNextItem(currentPlaylist)
            if (state.currentPlaylist?.currentIndex ?: 0 < currentPlaylist.items.size) {
                updateCurrentItem()
            }
        }
    }

    override fun previousItem() {
        state.currentPlaylist?.let { currentPlaylist ->
            state.currentPlaylist = playlistMutator.gotoPreviousItem(currentPlaylist)
            updateCurrentItem()
        }
    }

    private fun updateCurrentItem() {
        state.currentPlaylistItem =
            state.currentPlaylist!!.items[state.currentPlaylist!!.currentIndex]
        state.currentPlaylistItem?.apply {
            mediaSessionManager.setMedia(media)
        }
        consumerListeners.forEach { it.onItemChanged() }
    }

    override fun getPlaylist(): PlaylistDomain? = state.currentPlaylist

    override fun getCurrentItem(): PlaylistItemDomain? = state.currentPlaylistItem

    override fun removeItem(playlistItemDomain: PlaylistItemDomain) {
        state.jobs.add(contextProvider.MainScope.launch {
            repository.delete(playlistItemDomain.media)
            refreshQueue()
        })
    }

    override fun itemIndex(item: PlaylistItemDomain): Int? =
        state.currentPlaylist
            ?.items
            ?.indexOfFirst { it.media.mediaId == item.media.mediaId }

    override fun onTrackEnded(media: MediaDomain?) {
        nextItem()
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        state.currentPlaylist = state.currentPlaylist?.let {
            playlistMutator.moveItem(it, fromPosition, toPosition)
            // todo save playlist
        }
    }

    override fun getItemFor(url: String): PlaylistItemDomain? {
        return state.currentPlaylist
            ?.items
            ?.firstOrNull() { it.media.url == url }
    }

    override fun refreshQueueBackground(after: (() -> Unit)?) {
        state.jobs.add(contextProvider.MainScope.launch {
            refreshQueue(after)
        })
    }

    override suspend fun refreshQueue(after: (() -> Unit)?) {
        repository
            .loadList(null)
            .takeIf { it.isSuccessful && it is RepoResult.Data }
            ?.let { state.mediaList = it.data!!; it.data }
            ?.map { mediaMapper.mapToPlaylistItem(it) }
            ?.let {
                PlaylistDomain(
                    title = "Media",
                    items = it,
                    currentIndex = state.currentPlaylist?.currentIndex ?: -1
                )
            }
            ?.also { state.currentPlaylist = it }
            ?.also { playlist ->
                producerListeners.forEach { l -> l.onPlaylistUpdated(playlist) }
            }?.also {
                after?.invoke()
            }
    }
}