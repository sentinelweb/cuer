package uk.co.sentinelweb.cuer.app.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_LOCAL_SEARCH
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.SearchDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearch
import uk.co.sentinelweb.cuer.domain.ext.serialise

class SearchViewModel(
    private val state: SearchContract.State,
    private val mapper: SearchMapper,
    private val log: LogWrapper,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>
) : ViewModel() {

    init {
        state.search = prefsWrapper
            .getString(LAST_LOCAL_SEARCH, null)
            ?.let { deserialiseSearch(it) }
            ?: SearchDomain()
    }

    var model: SearchContract.Model by mutableStateOf(mapper.map(state))
        private set

    private val _dialogModelLiveData: MutableLiveData<DialogModel> = MutableLiveData()
    fun getDialogObservable(): LiveData<DialogModel> = _dialogModelLiveData
    private val _navigateLiveData: MutableLiveData<NavigationModel> = MutableLiveData()
    fun getNavigationObservable(): LiveData<NavigationModel> = _navigateLiveData

    fun onSearchTextChange(text: String) {
        state.search.localParams.text = text
        model = mapper.map(state)
    }

    fun onWatchedClick(isWatched: Boolean) {
        state.search.localParams = state.search.localParams.copy(isWatched = isWatched)
        model = mapper.map(state)
    }

    fun onNewClick(isNew: Boolean) {
        state.search.localParams = state.search.localParams.copy(isNew = isNew)
        model = mapper.map(state)
    }

    fun onLiveClick(isLive: Boolean) {
        state.search.localParams = state.search.localParams.copy(isLive = isLive)
        model = mapper.map(state)
    }

    fun onSubmit() {
        prefsWrapper.putString(LAST_LOCAL_SEARCH, state.search.serialise())
        _navigateLiveData.value = NavigationModel(
            NavigationModel.Target.PLAYLIST_FRAGMENT,
            mapOf(
                NavigationModel.Param.PLAYLIST_ID to PlaylistMemoryRepository.LOCAL_SEARCH_PLAYLIST,
                NavigationModel.Param.PLAY_NOW to false,
                NavigationModel.Param.SOURCE to OrchestratorContract.Source.MEMORY
            )
        )
        log.d("Execute search ... ${state.search.localParams.text}")
    }

    fun onPlaylistSelect(@Suppress("UNUSED_PARAMETER") chipModel: ChipModel) {
        if (chipModel.type == PLAYLIST_SELECT) {
            _dialogModelLiveData.value =
                PlaylistsDialogContract.Config(
                    state.search.localParams.playlists,
                    true,
                    this@SearchViewModel::onPlaylistSelected,
                    { },
                    this@SearchViewModel::onPlaylistDialogClose,
                    showAdd = false
                )
        } else if (chipModel.type == PLAYLIST) {
            state.search.localParams.playlists
                .removeIf { it.id == chipModel.value?.toLong() }
                .also { model = mapper.map(state) }
        }
    }

    fun onPlaylistSelected(playlist: PlaylistDomain?, checked: Boolean) {
        playlist
            ?.apply {
                if (checked) {
                    state.search.localParams.playlists.add(this)
                } else {
                    state.search.localParams.playlists.remove(this)
                }
            }
            ?.also { model = mapper.map(state) }
            ?: throw IllegalStateException("")
    }

    fun onPlaylistDialogClose() {
        model = mapper.map(state)
    }

}