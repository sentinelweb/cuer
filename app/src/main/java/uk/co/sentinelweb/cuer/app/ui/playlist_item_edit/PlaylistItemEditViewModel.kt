package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.exception.NoDefaultPlaylistException
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.playlist.PlaylistSelectDialogModelCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.LINK
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.MEDIA_ID
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.LOCAL_PLAYER
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.WEB_LINK
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator


class PlaylistItemEditViewModel constructor(
    private val state: PlaylistItemEditState,
    private val modelMapper: PlaylistItemEditModelMapper,
    private val playlistRepo: PlaylistDatabaseRepository,
    private val playlistDialogModelCreator: PlaylistSelectDialogModelCreator,
    private val mediaRepo: MediaDatabaseRepository,
    private val itemCreator: PlaylistItemCreator,
    private val log: LogWrapper
) : ViewModel() {

    private val _modelLiveData: MutableLiveData<PlaylistItemEditModel> = MutableLiveData()
    private val _selectModelLiveData: MutableLiveData<DialogModel> = MutableLiveData()
    private val _navigateLiveData: MutableLiveData<NavigationModel> = MutableLiveData()

    fun getModelObservable(): LiveData<PlaylistItemEditModel> = _modelLiveData
    fun getNavigationObservable(): LiveData<NavigationModel> = _navigateLiveData
    fun getDialogObservable(): LiveData<DialogModel> = _selectModelLiveData

    @Suppress("RedundantOverride") // for note
    override fun onCleared() {
        super.onCleared()
        // https://developer.android.com/topic/libraries/architecture/coroutines
        // coroutines cancel via viewModelScope
    }

    fun setData(media: MediaDomain?) {
        // todo choose default playlist(s) - default flag or most recent
        viewModelScope.launch {
            media?.let {
                state.media = media
                media.id?.let {
                    state.selectedPlaylists.addAll(getPlaylistsForMediaId(it))
                }
                update()
            } ?: run {
                _modelLiveData.value = modelMapper.mapEmpty()
            }
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
        viewModelScope.launch {
            playlistDialogModelCreator.loadPlaylists {
                // todo prioritize ordering by usage
                state.allPlaylists = it
                _selectModelLiveData.value =
                    playlistDialogModelCreator.mapPlaylistSelectionForDialog(
                        it, state.selectedPlaylists, true,
                        this@PlaylistItemEditViewModel::onPlaylistSelected,
                        { },
                        this@PlaylistItemEditViewModel::onPlaylistDialogClose
                    )
            }
        }
    }

    fun onPlaylistSelected(index: Int, checked: Boolean) {
        if (index < state.allPlaylists?.size ?: 0) {
            state.allPlaylists?.get(index)?.apply {
                if (checked) {
                    state.selectedPlaylists.add(this)
                } else {
                    state.selectedPlaylists.remove(this)
                }
            }?.also { update() }
        } else {
            _selectModelLiveData.value =
                DialogModel(DialogModel.Type.PLAYLIST_ADD, "Create playlist")
        }
    }

    fun onPlaylistSelected(domain: PlaylistDomain) {
        state.selectedPlaylists.add(domain)
        update()
    }

    fun onPlaylistDialogClose() {
        update()
    }

    fun onRemovePlaylist(chipModel: ChipModel) {
        state.selectedPlaylists.find { it.id == chipModel.value?.toLong() }?.apply {
            state.selectedPlaylists.remove(this)
        }?.also { update() }
    }

    private fun update() {
        state.model = modelMapper.map(state.media!!, state.selectedPlaylists)
        _modelLiveData.value = state.model
    }

    suspend fun commitPlaylistItems() {
        val selectedPlaylists = if (state.selectedPlaylists.size > 0) {
            state.selectedPlaylists
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
                    playlistRepo.savePlaylistItem(
                        itemCreator.buildPlayListItem(savedMedia, playlist)
                    ).data
                }
            } ?: listOf()
    }

    fun getCommittedItems() = state.committedItems ?: listOf()

}
