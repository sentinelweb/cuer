package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.Navigate.LOCAL_PLAYER
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.Navigate.WEB_LINK
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.LINK
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.MEDIA_ID
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.domain.MediaDomain


class PlaylistItemEditViewModel constructor(
    private val state: PlaylistItemEditState,
    private val modelMapper: PlaylistItemEditModelMapper,
    private val playlistRepo: PlaylistDatabaseRepository
) : ViewModel() {

    private val _modelLiveData: MutableLiveData<PlaylistItemEditModel> = MutableLiveData()
    private val _selectModelLiveData: MutableLiveData<SelectDialogModel> = MutableLiveData()
    private val _navigateLiveData: MutableLiveData<NavigationModel> = MutableLiveData()

    fun getModelObservable(): LiveData<PlaylistItemEditModel> = _modelLiveData
    fun getNavigationObservable(): LiveData<NavigationModel> = _navigateLiveData
    fun getDialogObservable(): LiveData<SelectDialogModel> = _selectModelLiveData

    @Suppress("RedundantOverride") // for note
    override fun onCleared() {
        super.onCleared()
        // https://developer.android.com/topic/libraries/architecture/coroutines
        // coroutines cancel via viewModelScope
    }

    fun setData(media: MediaDomain?) {
        media?.let {
            state.media = media
            state.media.also { update() }
        } ?: run {
            _modelLiveData.value = modelMapper.mapEmpty()
        }
    }

    fun onPlayVideoLocal() {
        _navigateLiveData.value =
            NavigationModel(LOCAL_PLAYER, mapOf(MEDIA_ID to state.media!!.remoteId))
    }

    fun onStarClick() {
        state.media = state.media
            ?.let { it.copy(starred = !it.starred) }
            ?.also { update() }
    }

    fun onLinkClick(urlString: String) {
        _navigateLiveData.value =
            NavigationModel(WEB_LINK, mapOf(LINK to urlString))
    }

    fun onSelectPlaylist(@Suppress("UNUSED_PARAMETER") model: ChipModel) {
        viewModelScope.launch {
            playlistRepo.loadList(null)
                .takeIf { it.isSuccessful }
                ?.data?.apply {
                    state.allPlaylists = this
                    _selectModelLiveData.value =
                        modelMapper.mapSelection(this, state.selectedPlaylists)
                }

        }
    }

    fun onPlaylistSelected(index: Int, checked: Boolean) {
        state.allPlaylists?.get(index)?.apply {
            if (checked) {
                state.selectedPlaylists.add(this)
            } else {
                state.selectedPlaylists.remove(this)
            }
        }?.also { update() }
    }

    fun onPlaylistDialogClose() {
        update()
    }

    fun onRemovePlaylist(chipModel: ChipModel) {
        state.selectedPlaylists.find { it.id == chipModel.value }?.apply {
            state.selectedPlaylists.remove(this)
        }?.also { update() }
    }

    private fun update() {
        state.model = modelMapper.map(state.media!!, state.selectedPlaylists)
        _modelLiveData.value = state.model
    }

}
