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
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel.Type.PLAYLIST_ADD
import uk.co.sentinelweb.cuer.app.ui.common.dialog.playlist.PlaylistSelectDialogModelCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditViewModel.UiEvent.Type.REFRESHING
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*


class PlaylistItemEditViewModel constructor(
    private val state: PlaylistItemEditContract.State,
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
    private val _modelLiveData: MutableLiveData<PlaylistItemEditContract.Model> = MutableLiveData()
    private val _selectModelLiveData: MutableLiveData<DialogModel> = MutableLiveData()
    private val _navigateLiveData: MutableLiveData<NavigationModel> = MutableLiveData()

    fun getUiObservable(): LiveData<UiEvent> = _uiLiveData
    fun getModelObservable(): LiveData<PlaylistItemEditContract.Model> = _modelLiveData
    fun getNavigationObservable(): LiveData<NavigationModel> = _navigateLiveData
    fun getDialogObservable(): LiveData<DialogModel> = _selectModelLiveData

    private val isNew: Boolean
        get() = state.editingPlaylistItem?.id == null

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

    fun setData(item: PlaylistItemDomain) {
        item.let {
            state.editingPlaylistItem = it
            it.media.let { setData(it) }
        }
    }

    private fun setData(media: MediaDomain?) {
        viewModelScope.launch {
            state.isMediaChanged = media?.id == null
            media?.let { originalMedia ->
                state.media = originalMedia
                playlistDialogModelCreator.loadPlaylists { state.allPlaylists = it }
                originalMedia.id?.let {
                    playlistItemOrchestrator.loadList(MediaIdListFilter(listOf(it)), Options(LOCAL))
                        ?.takeIf { it.size > 0 }
                        ?.also { if (isNew) state.editingPlaylistItem = it[0] }
                        ?.also {
                            it.map { it.playlistId }
                                .distinct()
                                .filterNotNull()
                                .also {
                                    playlistOrchestrator.loadList(IdListFilter(it), Options(LOCAL))
                                        ?.also { state.selectedPlaylistIds.addAll(it.map { it.id!! }) }
                                }
                        }
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

    fun refreshMediaBackground() = viewModelScope.launch {
        refreshMedia()
    }

    private suspend fun refreshMedia() =
        withContext(viewModelScope.coroutineContext) {
            state.media?.let { originalMedia ->
                _uiLiveData.value = UiEvent(REFRESHING, true)
                mediaOrchestrator.load(originalMedia.platformId, Options(PLATFORM))
                    ?.let {
                        it.copy(
                            id = originalMedia.id,
                            dateLastPlayed = originalMedia.dateLastPlayed,
                            starred = originalMedia.starred,
                            watched = originalMedia.watched,
                        )
                            .also { state.media = it }
                            .also { state.isMediaChanged = true }
                            .also { update() }
                    }
                    ?: also { updateError() }
            }
        }

//    private suspend fun getPlaylistsForMediaId(mediaId: Long): List<PlaylistDomain> =
//
//            //?.also { log.d("mediaId = $mediaId, items = ${it.map { "[${it.id} ${it.media.id}  ${it.media.platformId} ]" }}") }
//            ?.map { it.playlistId }
//            ?.distinct()
//            ?.filterNotNull()
//            ?.let { playlistOrchestrator.loadList(IdListFilter(it), Options(LOCAL)) }
//            ?: listOf()

    fun onPlayVideo() {
        state.editingPlaylistItem?.let { item ->
            viewModelScope.launch {
                item.playlistId?.let {
                    queue.playNow(it.toIdentifier(LOCAL), item.id) // todo store source
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
            ?.also { state.isMediaChanged = true }
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
        state.isPlaylistsChanged = true
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
                DialogModel(PLAYLIST_ADD, R.string.create_playlist_dialog_title)
        }
    }

    fun onPlaylistSelected(domain: PlaylistDomain) {
        viewModelScope.launch {
            state.isPlaylistsChanged = true
            state.selectedPlaylistIds.add(domain.id!!)
            playlistDialogModelCreator.loadPlaylists { state.allPlaylists = it }
            update()
        }
    }

    fun onPlaylistDialogClose() {
        update()
    }

    fun onRemovePlaylist(chipModel: ChipModel) {
        state.isPlaylistsChanged = true
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

    fun checkToSave() {
        if (!state.isSaved && (state.isMediaChanged || state.isPlaylistsChanged)) {
            if (isNew) {
                _selectModelLiveData.value = modelMapper.mapSaveConfirmAlert({
                    viewModelScope.launch {
                        commitPlaylistItems()
                        _navigateLiveData.value = NavigationModel(NAV_DONE)
                    }
                }, {// cancel
                    _navigateLiveData.value = NavigationModel(NAV_DONE)
                })
            } else {
                // todo alert if taken off all playlists
                viewModelScope.launch {
                    commitPlaylistItems()
                    _navigateLiveData.value = NavigationModel(NAV_DONE)
                }
            }
        } else {
            _navigateLiveData.value = NavigationModel(NAV_DONE)
        }
    }

    suspend fun commitPlaylistItems() =
        try {
            val selectedPlaylists = if (state.selectedPlaylistIds.size > 0) {
                selectedPlaylists
            } else {
                playlistOrchestrator.loadList(DefaultFilter(), Options(LOCAL))
                    ?: throw NoDefaultPlaylistException()
            }
            if (state.isPlaylistsChanged && state.editingPlaylistItem?.playlistId != null) {
                state.editingPlaylistItem?.also { item ->
                    if (!state.selectedPlaylistIds.contains(item.playlistId)) {
                        playlistItemOrchestrator.delete(item, Options(LOCAL))// todo use identifier
                    }
                }
            }
            state.committedItems = state.media
                ?.let {
                    if (state.isMediaChanged) {
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
            state.isSaved = true
        } catch (e: Exception) {
            log.e("Error saving playlistItem", e)
        } finally {
            Unit
        }

    fun getCommittedItems() = state.committedItems

}
