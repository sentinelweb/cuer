package uk.co.sentinelweb.cuer.app.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class SearchViewModel(
    private val state: SearchContract.State,
    private val mapper: SearchMapper,
    private val log: LogWrapper
) : ViewModel() {

    var model: SearchContract.Model by mutableStateOf(mapper.map(state))
        private set

    private val _dialogModelLiveData: MutableLiveData<DialogModel> = MutableLiveData()
    fun getDialogObservable(): LiveData<DialogModel> = _dialogModelLiveData

    fun onSearchTextChange(text: String) {
        state.text = text
        model = mapper.map(state)
    }

    fun onWatchedClick(isWatched: Boolean) {
        state.localParams = state.localParams.copy(isWatched = isWatched)
        model = mapper.map(state)
    }

    fun onNewClick(isNew: Boolean) {
        state.localParams = state.localParams.copy(isNew = isNew)
        model = mapper.map(state)
    }

    fun onLiveClick(isLive: Boolean) {
        state.localParams = state.localParams.copy(isLive = isLive)
        model = mapper.map(state)
    }

    fun onSubmit() {
        log.d("Execute search ... ${state.text}")
    }

    fun onPlaylistSelect(@Suppress("UNUSED_PARAMETER") chipModel: ChipModel) {
        if (chipModel.type == PLAYLIST_SELECT) {
            _dialogModelLiveData.value =
                PlaylistsDialogContract.Config(
                    state.localParams.playlists,
                    true,
                    this@SearchViewModel::onPlaylistSelected,
                    { },
                    this@SearchViewModel::onPlaylistDialogClose,
                    showAdd = false
                )
        } else if (chipModel.type == PLAYLIST) {
            state.localParams.playlists
                .removeIf { it.id == chipModel.value?.toLong() }
                .also { model = mapper.map(state) }
        }
    }

    fun onPlaylistSelected(playlist: PlaylistDomain?, checked: Boolean) {
        playlist
            ?.apply {
                if (checked) {
                    state.localParams.playlists.add(this)
                } else {
                    state.localParams.playlists.remove(this)
                }
            }
            ?.also { model = mapper.map(state) }
            ?: throw IllegalStateException("")
    }

    fun onPlaylistDialogClose() {
        model = mapper.map(state)
    }

}