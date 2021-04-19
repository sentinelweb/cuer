package uk.co.sentinelweb.cuer.app.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.LOCAL_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.REMOTE_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_LOCAL_SEARCH
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_REMOTE_SEARCH
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.SearchLocalDomain
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearchLocal
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearchRemote
import uk.co.sentinelweb.cuer.domain.ext.serialise

class SearchViewModel(
    private val state: SearchContract.State,
    private val mapper: SearchMapper,
    private val log: LogWrapper,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>
) : ViewModel() {

    init {
        state.local = prefsWrapper
            .getString(LAST_LOCAL_SEARCH, null)
            ?.let { deserialiseSearchLocal(it) }
            ?: SearchLocalDomain()
        state.remote = prefsWrapper
            .getString(LAST_REMOTE_SEARCH, null)
            ?.let { deserialiseSearchRemote(it) }
            ?: SearchRemoteDomain()
    }

    var model: SearchContract.Model by mutableStateOf(mapper.map(state))
        private set

    private val _dialogModelLiveData: MutableLiveData<DialogModel> = MutableLiveData()
    fun getDialogObservable(): LiveData<DialogModel> = _dialogModelLiveData
    private val _navigateLiveData: MutableLiveData<NavigationModel> = MutableLiveData()
    fun getNavigationObservable(): LiveData<NavigationModel> = _navigateLiveData

    fun onSearchTextChange(text: String) {
        if (state.isLocal) {
            state.local.text = text
        } else {
            state.remote.text = text
        }
        model = mapper.map(state)
    }

    fun switchLocalOrRemote() {
        state.isLocal = !state.isLocal
        model = mapper.map(state)
    }

    fun onWatchedClick(isWatched: Boolean) {
        state.local = state.local.copy(isWatched = isWatched)
        model = mapper.map(state)
    }

    fun onNewClick(isNew: Boolean) {
        state.local = state.local.copy(isNew = isNew)
        model = mapper.map(state)
    }

    fun onLiveClick(isLive: Boolean) {
        state.local = state.local.copy(isLive = isLive)
        model = mapper.map(state)
    }

    fun onSubmit() {
        if (state.isLocal) {
            prefsWrapper.putString(LAST_LOCAL_SEARCH, state.local.serialise())
        } else {
            prefsWrapper.putString(LAST_REMOTE_SEARCH, state.remote.serialise())
        }
        _navigateLiveData.value = NavigationModel(
            NavigationModel.Target.PLAYLIST_FRAGMENT,
            mapOf(
                NavigationModel.Param.PLAYLIST_ID to if (state.isLocal) LOCAL_SEARCH_PLAYLIST else REMOTE_SEARCH_PLAYLIST,
                NavigationModel.Param.PLAY_NOW to false,
                NavigationModel.Param.SOURCE to OrchestratorContract.Source.MEMORY
            )
        )
        log.d("Execute search ... ${state.local.text}")
    }

    fun onPlaylistSelect(@Suppress("UNUSED_PARAMETER") chipModel: ChipModel) {
        if (chipModel.type == PLAYLIST_SELECT) {
            _dialogModelLiveData.value =
                PlaylistsDialogContract.Config(
                    state.local.playlists,
                    true,
                    this@SearchViewModel::onPlaylistSelected,
                    { },
                    this@SearchViewModel::onPlaylistDialogClose,
                    showAdd = false
                )
        } else if (chipModel.type == PLAYLIST) {
            state.local.playlists
                .removeIf { it.id == chipModel.value?.toLong() }
                .also { model = mapper.map(state) }
        }
    }

    fun onPlaylistSelected(playlist: PlaylistDomain?, checked: Boolean) {
        playlist
            ?.apply {
                if (checked) {
                    state.local.playlists.add(this)
                } else {
                    state.local.playlists.remove(this)
                }
            }
            ?.also { model = mapper.map(state) }
            ?: throw IllegalStateException("")
    }

    fun onPlaylistDialogClose() {
        model = mapper.map(state)
    }

}