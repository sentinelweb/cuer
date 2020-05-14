package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.Navigate.LOCAL_PLAYER
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.Navigate.WEB_LINK
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.LINK
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.MEDIA_ID
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.domain.MediaDomain

class PlaylistItemEditViewModel constructor(
    private val state: PlaylistItemEditState,
    private val modelMapper: PlaylistItemEditModelMapper
) : ViewModel() {

    private val _modelLiveData: MutableLiveData<PlaylistItemEditModel> = MutableLiveData()
    private val _navigateLiveData: MutableLiveData<NavigationModel> =
        MutableLiveData()

    fun getModelObservable(): LiveData<PlaylistItemEditModel> = _modelLiveData
    fun getNavigationObservable(): LiveData<NavigationModel> =
        _navigateLiveData

    fun setData(media: MediaDomain) {
        state.media = media.also {
            state.model = modelMapper.map(it)
            _modelLiveData.value = state.model
        }
    }

    fun onPlayVideoLocal() {
        _navigateLiveData.value =
            NavigationModel(LOCAL_PLAYER, mapOf(MEDIA_ID to state.media!!.mediaId))
    }

    fun onStarClick() {
        state.media = state.media
            ?.let { it.copy(starred = !it.starred) }
            ?.also {
                state.model = modelMapper.map(it)
                _modelLiveData.value = state.model
            }
    }

    fun onLinkClick(urlString: String) {
        _navigateLiveData.value =
            NavigationModel(WEB_LINK, mapOf(LINK to urlString))
    }

    fun onSelectPlaylist(model: ChipModel) {

    }

}
