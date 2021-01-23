package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.exception.NoDefaultPlaylistException
import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.PLATFORM
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.playlist.PlaylistSelectDialogModelCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*


class PlaylistItemEditViewModel constructor(
    private val state: PlaylistItemEditState,
    private val modelMapper: PlaylistItemEditModelMapper,
//    private val playlistRepo: PlaylistDatabaseRepository,
    private val playlistDialogModelCreator: PlaylistSelectDialogModelCreator,
//    private val mediaRepo: MediaDatabaseRepository,
    private val itemCreator: PlaylistItemCreator,
    private val log: LogWrapper,
//    private val ytInteractor: YoutubeInteractor,
    private val queue: QueueMediatorContract.Producer,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val toast: ToastWrapper,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator
) : ViewModel() {

    data class UiEvent(
        val type: Type,
        val data: Any?
    ) {
        enum class Type { REFRESHING }
    }

    private val _uiLiveData: MutableLiveData<UiEvent> = MutableLiveData()
    private val _modelLiveData: MutableLiveData<PlaylistItemEditModel> = MutableLiveData()
    private val _selectModelLiveData: MutableLiveData<DialogModel> = MutableLiveData()
    private val _navigateLiveData: MutableLiveData<NavigationModel> = MutableLiveData()

    fun getUiObservable(): LiveData<UiEvent> = _uiLiveData
    fun getModelObservable(): LiveData<PlaylistItemEditModel> = _modelLiveData
    fun getNavigationObservable(): LiveData<NavigationModel> = _navigateLiveData
    fun getDialogObservable(): LiveData<DialogModel> = _selectModelLiveData

    private val selectedPlaylists: Set<PlaylistDomain>
        get() = state.allPlaylists.filter { state.selectedPlaylistIds.contains(it.id) }.toSet()

    @Suppress("RedundantOverride") // for note
    override fun onCleared() {
        super.onCleared()
        // https://developer.android.com/topic/libraries/architecture/coroutines
        // coroutines cancel via viewModelScope
    }

    fun delayedSetData(item: PlaylistItemDomain) {
        viewModelScope.launch {
            delay(400)
            setData(item) // loads data after delay
        }
    }

    fun setData(media: MediaDomain?) {
        viewModelScope.launch {
            state.mediaChanged = media?.id != null
            media?.let { originalMedia ->
                state.media = originalMedia
                playlistDialogModelCreator.loadPlaylists { state.allPlaylists = it }
                originalMedia.id?.let {
                    state.selectedPlaylistIds.addAll(getPlaylistsForMediaId(it).map { it.id!! })
                }
                if (originalMedia.channelData.thumbNail == null
                    || (originalMedia.duration?.let { it > 1000 * 60 * 60 * 24 } ?: false)
                    || originalMedia.isLiveBroadcast
                    || originalMedia.isLiveBroadcastUpcoming
                ) {
                    refreshMedia()
                } else {
                    update()
                }
            } ?: run {
                _modelLiveData.value = modelMapper.mapEmpty()
            }
        }
    }

    fun setData(item: PlaylistItemDomain) {
        item.let {
            state.editingPlaylistItem = it
            it.media.let { setData(it) }
        }
    }

    fun refreshMediaBackground() = viewModelScope.launch {
        refreshMedia()
    }

    suspend fun refreshMedia() =
        withContext(viewModelScope.coroutineContext) {
            state.media?.let { originalMedia ->
                _uiLiveData.value = PlaylistItemEditViewModel.UiEvent(PlaylistItemEditViewModel.UiEvent.Type.REFRESHING, true)
                mediaOrchestrator.load(originalMedia.platformId, Options(PLATFORM))
                    ?.let {
                        it.copy(
                            id = originalMedia.id,
                            dateLastPlayed = originalMedia.dateLastPlayed,
                            starred = originalMedia.starred,
                            watched = originalMedia.watched,
                        )
                            .also { state.media = it }
                            .also { state.mediaChanged = true }
                            .also { update() }
                    }
                    ?: also { updateError() }
            }
        }

    private suspend fun getPlaylistsForMediaId(mediaId: Long): List<PlaylistDomain> =
        playlistItemOrchestrator.loadList(MediaIdListFilter(listOf(mediaId)), Options(LOCAL))
            ?.let { playlistOrchestrator.loadList(IdListFilter(it.map { it.playlistId!! }), Options(LOCAL)) }
            ?: listOf()

    fun onPlayVideo() {
        state.editingPlaylistItem?.let { item ->
            viewModelScope.launch {
                item.playlistId?.let {
                    queue.playNow(it, item.id)
                }
            }
            if (!ytContextHolder.isConnected()) {
                _selectModelLiveData.value = DialogModel(DialogModel.Type.SELECT_ROUTE, R.string.select_route_dialog_title)
            }
        } ?: run { toast.show("Please save the item first ...") }

// need different UX for local
//        _navigateLiveData.value =
//            NavigationModel(
//                LOCAL_PLAYER,
//                mapOf(MEDIA_ID to state.media!!.platformId)
//            )
    }

    fun onStarClick() {
        state.media
            ?.let { it.copy(starred = !it.starred) }
            ?.also { state.media = it }
            ?.also { state.mediaChanged = true }
            ?.also { update() }
    }

    fun onLinkClick(urlString: String) {
        _navigateLiveData.value =
            NavigationModel(
                WEB_LINK,
                mapOf(LINK to urlString)
            )
    }

    fun onSelectPlaylistChipClick(@Suppress("UNUSED_PARAMETER") model: ChipModel) {
        _selectModelLiveData.value =
            playlistDialogModelCreator.mapPlaylistSelectionForDialog(
                state.allPlaylists,
                selectedPlaylists,
                true,
                this@PlaylistItemEditViewModel::onPlaylistSelected,
                { },
                this@PlaylistItemEditViewModel::onPlaylistDialogClose
            )
    }

    fun onPlaylistSelected(index: Int, checked: Boolean) {
        state.playlistsChanged = true
        if (index < state.allPlaylists.size) {
            state.allPlaylists.get(index).apply {
                if (checked) {
                    this.id?.let { state.selectedPlaylistIds.add(it) }
                } else {
                    this.id?.let { state.selectedPlaylistIds.remove(it) }
                }
            }.also { update() }
        } else {
            _selectModelLiveData.value =
                DialogModel(DialogModel.Type.PLAYLIST_ADD, R.string.create_playlist_dialog_title)
        }
    }

    fun onPlaylistSelected(domain: PlaylistDomain) {
        viewModelScope.launch {
            state.playlistsChanged = true
            state.selectedPlaylistIds.add(domain.id!!)
            playlistDialogModelCreator.loadPlaylists { state.allPlaylists = it }
            update()
        }
    }

    fun onPlaylistDialogClose() {
        update()
    }

    fun onRemovePlaylist(chipModel: ChipModel) {
        state.selectedPlaylistIds.remove(chipModel.value?.toLong())
        update()
    }

    fun onChannelClick() {
        state.media?.channelData?.platformId?.let { channelId ->
            _navigateLiveData.value = NavigationModel(YOUTUBE_CHANNEL, mapOf(CHANNEL_ID to channelId))
        }
    }

    private fun update() {
        state.model = modelMapper.map(state.media!!, selectedPlaylists)
        _modelLiveData.value = state.model
    }

    private fun updateError() {
        state.model = modelMapper.mapEmpty()
        _modelLiveData.value = state.model
    }

    fun checkToSave() { // todo rewrite for share
        viewModelScope.launch {
            commitPlaylistItems()
            _navigateLiveData.value = NavigationModel(NAV_BACK)
        }
    }
    //        state.playlistItem?.also { item ->
    //            viewModelScope.launch {
    //                if (state.mediaChanged) {
    //                    state.media?.let { mediaOrchestrator.save(it, Options(LOCAL)) }
    //                }
    //                // todo merge below into commitPlaylistItems() - needs to work for existing from share
    //                if (state.playlistsChanged) {
    //                    if (!state.selectedPlaylistIds.contains(item.playlistId)) {
    //                        playlistItemOrchestrator.delete(item, DeleteOptions(LOCAL))
    //                    } else {
    //                        playlistItemOrchestrator.save(item, Options(LOCAL))
    //                    }
    //                    commitPlaylistItems()
    //                }
    //                _navigateLiveData.value = NavigationModel(NAV_BACK)
    //            }
    //        } ?: run { _navigateLiveData.value = NavigationModel(NAV_BACK) }

    suspend fun commitPlaylistItems(): List<PlaylistItemDomain> =
        try {
            val selectedPlaylists = if (state.selectedPlaylistIds.size > 0) {
                selectedPlaylists
            } else {
                playlistOrchestrator.loadList(DefaultFilter(), Options(LOCAL))
                    ?: throw NoDefaultPlaylistException()
            }
            if (state.playlistsChanged && state.editingPlaylistItem?.playlistId != null) {
                state.editingPlaylistItem?.also { item ->
                    if (!state.selectedPlaylistIds.contains(item.playlistId)) {
                        playlistItemOrchestrator.delete(item, DeleteOptions(LOCAL))
                    }
                }
            }
            state.committedItems = state.media
                ?.let {
                    if (state.mediaChanged) {
                        mediaOrchestrator.save(it, Options(LOCAL, flat = false))
                    } else it
                }
                ?.let { savedMedia ->
                    state.media = savedMedia
                    selectedPlaylists.mapNotNull { playlist ->
                        playlistItemOrchestrator.run {
                            save(itemCreator.buildPlayListItem(savedMedia, playlist), Options(LOCAL))
                        }

                    }
                } ?: listOf()
            state.committedItems!! // fixme
        } catch (e: Exception) {
            log.e("Error saving playlistItem", e)
            listOf()
        }


    fun getCommittedItems() = state.committedItems ?: listOf()

}
