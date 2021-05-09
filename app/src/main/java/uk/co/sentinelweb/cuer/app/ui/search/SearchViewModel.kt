package uk.co.sentinelweb.cuer.app.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.LOCAL_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.REMOTE_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DateRangePickerDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.EnumValuesDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchContract.SearchType.LOCAL
import uk.co.sentinelweb.cuer.app.ui.search.SearchContract.SearchType.REMOTE
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.*
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.SearchLocalDomain
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearchLocal
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearchRemote
import uk.co.sentinelweb.cuer.domain.ext.serialise
import java.time.LocalDateTime

class SearchViewModel(
    private val state: SearchContract.State,
    private val mapper: SearchMapper,
    private val log: LogWrapper,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>,
    private val timeStampMapper: TimeStampMapper
) : ViewModel() {

    init {
        state.searchType = prefsWrapper.getEnum(LAST_SEARCH_TYPE, LOCAL)
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
        when (state.searchType) {
            LOCAL -> state.local.text = text
            REMOTE -> {
                state.remote.text = text
                state.remote.relatedToMediaPlatformId = null
                state.remote.relatedToMediaTitle = null
            }
        }
        model = mapper.map(state)
    }

    fun switchLocalOrRemote() {
        state.searchType = when (state.searchType) {
            LOCAL -> REMOTE
            REMOTE -> LOCAL
        }
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
        when (state.searchType) {
            LOCAL -> state.local.isLive = isLive
            REMOTE -> state.remote.isLive = isLive
        }
        model = mapper.map(state)
    }

    fun onClearRelated() {
        state.remote.relatedToMediaPlatformId = null
        state.remote.relatedToMediaTitle = null
        model = mapper.map(state)
    }

    fun onClearDates() {
        state.remote = state.remote.copy(fromDate = null, toDate = null)
        model = mapper.map(state)
    }

    fun onSelectDates() {
        _dialogModelLiveData.value =
            DateRangePickerDialogModel(
                R.string.search_select_dates,
                state.remote.fromDate,
                state.remote.toDate ?: LocalDateTime.now(),
                this::onDatesSelected,
                { _dialogModelLiveData.value = DialogModel.DismissDialogModel() }
            )
    }

    fun onDatesSelected(start: Long, end: Long) {
        state.remote = state.remote.copy(
            fromDate = timeStampMapper.toLocalDateTimeNano(start),
            toDate = timeStampMapper.toLocalDateTimeNano(end)
        )
        model = mapper.map(state)
    }

    fun onSelectOrder() {
        log.d("Select order")
        _dialogModelLiveData.value =
            EnumValuesDialogModel(
                R.string.search_select_dates,
                SearchRemoteDomain.Order.values().toList(),
                state.remote.order,
                this::onOrderSelected,
                { }
            )
    }

    fun onOrderSelected(order: SearchRemoteDomain.Order) {
        state.remote = state.remote.copy(order = order)
        model = mapper.map(state)
    }

    fun onSubmit() {
        when (state.searchType) {
            LOCAL -> {
                prefsWrapper.putString(LAST_LOCAL_SEARCH, state.local.serialise())
                prefsWrapper.putEnum(LAST_SEARCH_TYPE, LOCAL)
            }
            REMOTE -> {
                prefsWrapper.putString(LAST_REMOTE_SEARCH, state.remote.serialise())
                prefsWrapper.putEnum(LAST_SEARCH_TYPE, REMOTE)
            }
        }

        _navigateLiveData.value = NavigationModel(
            NavigationModel.Target.PLAYLIST_FRAGMENT,
            mapOf(
                NavigationModel.Param.PLAYLIST_ID to if (state.searchType == LOCAL) LOCAL_SEARCH_PLAYLIST else REMOTE_SEARCH_PLAYLIST,
                NavigationModel.Param.PLAY_NOW to false,
                NavigationModel.Param.SOURCE to OrchestratorContract.Source.MEMORY
            )
        )
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
                    showAdd = false,
                    showPin = false
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