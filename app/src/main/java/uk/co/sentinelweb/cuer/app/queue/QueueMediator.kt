package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract.ConsumerListener
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract.ProducerListener
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.CURRENT_PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.currentItem
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

class QueueMediator constructor(
    private val state: QueueMediatorState,
    private val repository: MediaDatabaseRepository,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val mediaMapper: MediaToPlaylistItemMapper,
    private val contextProvider: CoroutineContextProvider,
    private val mediaSessionManager: MediaSessionManager,
    private val playlistMutator: PlaylistMutator,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>
) : QueueMediatorContract.Producer, QueueMediatorContract.Consumer {

    private val consumerListeners: MutableList<ConsumerListener> = mutableListOf()
    private val producerListeners: MutableList<ProducerListener> = mutableListOf()

    override val currentItem: PlaylistItemDomain?
        get() = state.currentItem
    override val currentItemIndex: Int?
        get() = state.currentItem?.let { item -> state.playlist?.items?.indexOfFirst { item.id == it.id } }
    override val playlist: PlaylistDomain?
        get() = state.playlist

    init {
        state.playlistId = prefsWrapper.getLong(CURRENT_PLAYLIST_ID)
        refreshQueueBackground()
    }

    override fun onItemSelected(playlistItem: PlaylistItemDomain) {
        state.playlist
            ?.takeIf { playlistItem != state.currentItem }
            ?.let {
                state.playlist = playlistMutator.playItem(it, playlistItem)
                updateCurrentItem()
            }
    }

    override fun playNow() {
        state.playlist?.apply {
            val itemToPlay = if (currentIndex == -1) {
                items[0]
            } else if (currentIndex >= items.size) {
                items[0]
            } else {
                currentItem()
            }
            itemToPlay?.let {
                state.playlist = playlistMutator.playItem(this, it)
                updateCurrentItem()
            }
        }
    }

    override fun updateMediaItem(updatedMedia: MediaDomain) {
        state.currentItem = state.currentItem?.run {
            val mediaUpdated = media.copy(
                positon = updatedMedia.positon,
                duration = updatedMedia.duration,
                watched = true
            )
            exec { repository.save(mediaUpdated, true) }
            copy(media = mediaUpdated)
        }
    }

    override fun addConsumerListener(l: ConsumerListener) {
        consumerListeners.add(l)
    }

    override fun removeConsumerListener(l: ConsumerListener) {
        consumerListeners.remove(l)
    }

    override fun addProducerListener(l: ProducerListener) {
        producerListeners.add(l)
    }

    override fun removeProducerListener(l: ProducerListener) {
        producerListeners.remove(l)
    }

    override fun itemRemoved(playlistItemDomain: PlaylistItemDomain) {
        val currentItemDeleted = currentItem?.id == playlistItemDomain.id
        state.playlist = state.playlist?.let { plist ->
            plist.copy(items = plist.items.let {
                plist.items.find { it.id == playlistItemDomain.id }?.apply {
                    it.minus(this)
                }
                it
            })
        }
        if (currentItemDeleted) {
            nextItem()
        }
    }

    override fun destroy() {
        // might not be needed if singleton
        // save queue position
    }

    override fun nextItem() {
        state.playlist?.let { currentPlaylist ->
            state.playlist = playlistMutator.gotoNextItem(currentPlaylist)
            if (state.playlist?.currentIndex ?: 0 < currentPlaylist.items.size) {
                updateCurrentItem()
            }
        }
    }

    override fun previousItem() {
        state.playlist?.let { currentPlaylist ->
            state.playlist = playlistMutator.gotoPreviousItem(currentPlaylist)
            updateCurrentItem()
        }
    }

    private fun updateCurrentItem() {
        state.currentItem = state.playlist
            ?.let { playlist ->
                exec { playlistRepository.save(playlist, true) }
                playlist.currentIndex.let { playlist.items[it] }
            }
            ?: throw NullPointerException("playlist should not be null")
        state.currentItem?.apply {
            mediaSessionManager.setMedia(media)
        }
        consumerListeners.forEach { it.onItemChanged() }
        producerListeners.forEach { it.onItemChanged() }
    }

    override fun onTrackEnded(media: MediaDomain?) {
        nextItem()
    }

    override fun refreshQueueBackground() {
        exec { refreshQueue() }
    }

    fun exec(block: suspend () -> Unit) {
        state.jobs.add(contextProvider.MainScope.launch { block() })
    }

    override fun refreshQueueFrom(playlistDomain: PlaylistDomain) {
        // if the playlist is the same then don't change the current item
        if (state.playlist?.id != playlistDomain.id) {
            state.playlistId = playlistDomain.id
            state.currentItem = if (playlistDomain.currentIndex > -1) {
                playlistDomain.items[playlistDomain.currentIndex]
            } else null
        } else {
            state.currentItem = state.currentItem
                ?.let { item -> playlistDomain.items.find { it.id == item.id } }
        }
        state.playlist = playlistDomain
    }

    override suspend fun refreshQueue() {//after: (() -> Unit)?
        (state.playlistId
            ?.let { playlistRepository.load(it) }
            ?.takeIf { it.isSuccessful }
            ?.data
            ?: playlistRepository.loadList(PlaylistDatabaseRepository.DefaultFilter(flat = false))
                .takeIf { it.isSuccessful && it.data?.size ?: 0 > 0 }
                ?.data?.get(0))
            ?.also { refreshQueueFrom(it) }
            ?: throw IllegalStateException("Could not load a playlist")


//        repository
//            .loadList(null)
//            .takeIf { it.isSuccessful && it is RepoResult.Data }
//            ?.let { state.mediaList = it.data!!; it.data }
//            ?.map { mediaMapper.mapToPlaylistItem(it) }
//            ?.let {
//                PlaylistDomain(
//                    title = "Media",
//                    items = it,
//                    currentIndex = state.currentPlaylist?.currentIndex ?: -1
//                )
//            }
//            ?.also { state.currentPlaylist = it }
//            ?.also { playlist ->
//                producerListeners.forEach { l -> l.onPlaylistUpdated(playlist) }
//            }?.also {
//                after?.invoke()
//            }
    }

//    override fun itemIndex(item: PlaylistItemDomain): Int? =
//        state.currentPlaylist
//            ?.items
//            ?.indexOfFirst { it.media.platformId == item.media.platformId }

//    override fun getItemFor(url: String): PlaylistItemDomain? {
//        return state.currentPlaylist
//            ?.items
//            ?.firstOrNull() { it.media.url == url }
//    }

}