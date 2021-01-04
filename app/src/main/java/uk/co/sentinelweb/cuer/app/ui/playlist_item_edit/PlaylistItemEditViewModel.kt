package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.exception.NoDefaultPlaylistException
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.playlist.PlaylistSelectDialogModelCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor


class PlaylistItemEditViewModel constructor(
    private val state: PlaylistItemEditState,
    private val modelMapper: PlaylistItemEditModelMapper,
    private val playlistRepo: PlaylistDatabaseRepository,
    private val playlistDialogModelCreator: PlaylistSelectDialogModelCreator,
    private val mediaRepo: MediaDatabaseRepository,
    private val itemCreator: PlaylistItemCreator,
    private val log: LogWrapper,
    private val ytInteractor: YoutubeInteractor
) : ViewModel() {

    private val _modelLiveData: MutableLiveData<PlaylistItemEditModel> = MutableLiveData()
    private val _selectModelLiveData: MutableLiveData<DialogModel> = MutableLiveData()
    private val _navigateLiveData: MutableLiveData<NavigationModel> = MutableLiveData()

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

    fun delayedLoad(item: PlaylistItemDomain) {
        viewModelScope.launch {
            delay(400)
            setData(item) // loads data after delay
        }
    }

    fun setData(media: MediaDomain?) {
        viewModelScope.launch {
            media?.let { originalMedia ->
                state.media = originalMedia
                playlistDialogModelCreator.loadPlaylists { state.allPlaylists = it }
                originalMedia.id?.let {
                    state.selectedPlaylistIds.addAll(getPlaylistsForMediaId(it).map { it.id!! })
                }
                if (originalMedia.channelData.thumbNail == null) {
                    originalMedia.channelData.platformId?.apply {
                        ytInteractor.channels(listOf(this))
                            .takeIf { it.isSuccessful && (it.data?.size ?: 0) > 0 }
                            ?.let {
                                it.data?.get(0)?.apply {
                                    state.media = state.media?.copy(channelData = this.copy(id = originalMedia.channelData.id))
                                    state.mediaChanged = true
                                }
                            }
                    }
                }
                update()
            } ?: run {
                _modelLiveData.value = modelMapper.mapEmpty()
            }
        }
    }

    fun setData(item: PlaylistItemDomain) {
        item.let {
            state.playlistItem = it
            it.media.let { setData(it) }
        }
    }

    private suspend fun getPlaylistsForMediaId(mediaId: Long): List<PlaylistDomain> =
        playlistRepo.loadPlaylistItems(PlaylistDatabaseRepository.MediaIdListFilter(listOf(mediaId)))
            .takeIf { it.isSuccessful }
            ?.data
            ?.also { log.d("Playlist Items = ${it.map { it.playlistId }}") }
            ?.let { playlistRepo.loadList(PlaylistDatabaseRepository.IdListFilter(it.map { it.playlistId!! }, flat = false)) }
            ?.takeIf { it.isSuccessful }
            ?.data
            ?: listOf()


    fun onPlayVideoLocal() {
        _navigateLiveData.value =
            NavigationModel(
                LOCAL_PLAYER,
                mapOf(MEDIA_ID to state.media!!.platformId)
            )
    }

    fun onStarClick() {
        state.media = state.media
            ?.let { it.copy(starred = !it.starred) }
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
                DialogModel(DialogModel.Type.PLAYLIST_ADD, "Create playlist")
        }
    }

    fun onPlaylistSelected(domain: PlaylistDomain) {
        viewModelScope.launch {
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

    fun checkToSave() =
        state.playlistItem?.also { item ->
            viewModelScope.launch {
                if (state.mediaChanged) {
                    state.media?.let { mediaRepo.save(it) }
                }
                if (state.playlistsChanged) {
                    if (!state.selectedPlaylistIds.contains(item.playlistId)) {
                        playlistRepo.delete(item)
                    } else {
                        playlistRepo.savePlaylistItem(item)
                    }
                    commitPlaylistItems()
                }
                _navigateLiveData.value = NavigationModel(NAV_BACK)
            }
        } ?: run { _navigateLiveData.value = NavigationModel(NAV_BACK) }

    suspend fun commitPlaylistItems() {
        try {
            val selectedPlaylists = if (state.selectedPlaylistIds.size > 0) {
                selectedPlaylists
            } else {
                playlistRepo.loadList(PlaylistDatabaseRepository.DefaultFilter())
                    .takeIf { it.isSuccessful && it.data?.size ?: 0 > 0 }
                    ?.data
                    ?: throw NoDefaultPlaylistException()
            }
            state.committedItems = state.media
                ?.let { mediaRepo.save(it) }
                ?.takeIf { it.isSuccessful }
                ?.data?.let { savedMedia ->
                    state.media = savedMedia
                    selectedPlaylists.mapNotNull { playlist ->
                        playlistRepo
                            .takeIf { state.playlistItem?.playlistId != playlist.id } // skip edited item (saved in checkToSave)
                            ?.run {
                                savePlaylistItem(itemCreator.buildPlayListItem(savedMedia, playlist)).data
                            }

                    }
                } ?: listOf()
        } catch (e: Exception) {
            log.e("Error saving playlistItem", e)
        }
    }

    fun getCommittedItems() = state.committedItems ?: listOf()

}
