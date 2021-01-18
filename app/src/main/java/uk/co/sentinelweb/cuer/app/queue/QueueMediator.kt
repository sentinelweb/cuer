package uk.co.sentinelweb.cuer.app.queue

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract.ConsumerListener
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract.ProducerListener
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.CURRENT_PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.currentItem
import uk.co.sentinelweb.cuer.domain.ext.currentItemOrStart
import uk.co.sentinelweb.cuer.domain.ext.indexOfItemId
import uk.co.sentinelweb.cuer.domain.mutator.PlaylistMutator

class QueueMediator constructor(
    private val state: QueueMediatorState,
    private val repository: MediaDatabaseRepository,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val contextProvider: CoroutineContextProvider,
    private val mediaSessionManager: MediaSessionManager,
    private val playlistMutator: PlaylistMutator,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>,
    private val log: LogWrapper
) : QueueMediatorContract.Producer, QueueMediatorContract.Consumer {

    override val currentItem: PlaylistItemDomain?
        get() = state.currentItem
    override val currentItemIndex: Int?
        get() = state.currentItem?.let { item -> state.playlist?.items?.indexOfFirst { item.id == it.id } }
    override val playlist: PlaylistDomain?
        get() = state.playlist
    override val playlistId: Long?
        get() = state.playlistId

    private val consumerListeners: MutableList<ConsumerListener> = mutableListOf()
    private val producerListeners: MutableList<ProducerListener> = mutableListOf()

    init {
        log.tag(this)
        state.playlistId = prefsWrapper.getLong(CURRENT_PLAYLIST_ID)
        refreshQueueBackground()
    }

    override fun onItemSelected(playlistItem: PlaylistItemDomain, forcePlay: Boolean, resetPosition: Boolean) {
        state.playlist
            ?.takeIf { playlistItem != state.currentItem || forcePlay }
            ?.let {
                state.playlist = playlistMutator.playItem(it, playlistItem)
                updateCurrentItem(resetPosition)
            }
    }

    override suspend fun playNow(playlistId: Long, playlistItemId: Long?) {
        playlistRepository.load(playlistId, false)
            .takeIf { it.isSuccessful }
            ?.data
            ?.let {
                //log.d("playNow(loaded playlist= ${it.id}, requested=$playlistId, item = $playlistItemId)")
                playNow(it, playlistItemId)
            }
    }

    override suspend fun playNow(playlist: PlaylistDomain, playlistItemId: Long?) {
        playlist.indexOfItemId(playlistItemId)?.let { foundIndex ->
            //log.d("playNow(load found Index= $foundIndex)")
            playlist.let {
                it.copy(currentIndex = foundIndex).apply {
                    playlistRepository.save(it, false)
                }
            }
        }?.also {
            //log.d("playNow(updated Index to = ${it.currentIndex})")
            prefsWrapper.putLong(CURRENT_PLAYLIST_ID, it.id!!)
            refreshQueueFrom(it)
            //log.d("playNow(state current Index is = ${state.playlist?.currentIndex})")
            playNow()
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
            //log.d("playNow() item = ${itemToPlay})")
            itemToPlay?.let {
                //log.d("playNow(item= $it)")
                state.playlist = playlistMutator.playItem(this, it)
                updateCurrentItem(false)
            }
        }
    }

    override fun updateMediaItem(updatedMedia: MediaDomain) {
        state.currentItem = state.currentItem?.run {
            val mediaUpdated = media.copy(
                positon = updatedMedia.positon,
                duration = updatedMedia.duration,
                dateLastPlayed = updatedMedia.dateLastPlayed,
                watched = true
            )
            exec { repository.save(mediaUpdated, true) }
            copy(media = mediaUpdated)
        }
        state.playlist = state.playlist?.let {
            it.copy(items = it.items.toMutableList().apply {
                set(currentItemIndex ?: throw IllegalStateException(), state.currentItem ?: throw IllegalStateException())
            })
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

    override fun deleteItem(index: Int) {
        state.playlist?.let { plist ->
            val deleteItem = plist.items.get(index)
            val isCurrentItem = currentItem?.id == deleteItem.id
            exec {
                val mutated = playlistMutator.delete(plist, deleteItem)
                playlistRepository.delete(deleteItem)
                playlistRepository.updateCurrentIndex(mutated)
                refreshQueueFrom(mutated)
                if (isCurrentItem) {
                    if (mutated.currentIndex > -1) {
                        currentItem?.apply { onItemSelected(this, true, true) }
                    } else {
                        state.currentItem = null
                        consumerListeners.forEach { it.onItemChanged() }
                    }
                }
                withContext(contextProvider.Main) {
                    producerListeners.forEach { it.onPlaylistUpdated(mutated) }
                }
            }
        }
    }

    override fun refreshHeaderData() {
        exec {
            state.playlist?.let { plist ->
                plist.id?.let { id ->
                    playlistRepository.load(id, true)
                        .takeIf { it.isSuccessful }
                        ?.apply {
                            data?.apply {
                                refreshQueueFrom(
                                    copy(
                                        currentIndex = plist.currentIndex,
                                        items = plist.items
                                    )
                                )
                            }
                        }
                }
            }
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
                updateCurrentItem(false)
            }
        }
    }

    override fun previousItem() {
        state.playlist?.let { currentPlaylist ->
            state.playlist = playlistMutator.gotoPreviousItem(currentPlaylist)
            updateCurrentItem(false)
        }
    }

    private fun updateCurrentItem(resetPosition: Boolean) {
        state.currentItem = state.playlist
            ?.let { playlist ->
                exec { playlistRepository.save(playlist, true) }
                playlist.currentIndex.let { playlist.items[it] }
            }
            ?: throw NullPointerException("playlist should not be null")
        log.d("updateCurrentItem: currentItemId=${state.currentItem?.id} currentMediaId=${state.currentItem?.media?.id} currentIndex=${state.playlist?.currentIndex} items.size=${state.playlist?.items?.size} ")
        if (resetPosition) {
            state.currentItem?.apply { updateMediaItem(media.copy(positon = 0)) }
        }
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
        contextProvider.computationScope.launch { block() }
    }

    override fun refreshQueueFrom(playlistDomain: PlaylistDomain) {
        // if the playlist is the same then don't change the current item
        if (state.playlist?.id != playlistDomain.id) {
            state.playlistId = playlistDomain.id
            state.currentItem = playlistDomain.currentItemOrStart()
        } else {
            state.currentItem = playlistDomain.currentItem()
        }
        state.playlist = playlistDomain
        //log.d("refreshQueueFrom: currentItemId=${state.currentItem?.id}")
        state.currentItem?.apply {
            mediaSessionManager.setMedia(media)
        }
        contextProvider.mainScope.launch {
            consumerListeners.forEach { it.onPlaylistUpdated() }
        }
    }

    override suspend fun refreshQueue() {
        (state.playlistId
            ?.let { playlistRepository.load(it) }
            ?.takeIf { it.isSuccessful }
            ?.data
            ?: playlistRepository.loadList(PlaylistDatabaseRepository.DefaultFilter(flat = false))
                .takeIf { it.isSuccessful && it.data?.size ?: 0 > 0 }
                ?.data?.get(0))
            ?.also { refreshQueueFrom(it) }
            ?: throw IllegalStateException("Could not load a playlist")
    }

}