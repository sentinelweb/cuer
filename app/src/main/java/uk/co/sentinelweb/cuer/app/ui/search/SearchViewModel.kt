package uk.co.sentinelweb.cuer.app.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class SearchViewModel(
    private val state: SearchContract.State,
    private val mapper: SearchMapper,
    private val log: LogWrapper
) : ViewModel() {

    var model: SearchContract.Model by mutableStateOf(mapper.map(state))
        private set

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

    fun onPlaylistSelect(model: ChipModel) {
        log.d("Select playlists")
    }

    fun onSubmit() {
        log.d("Execute search ... ${state.text}")
    }
}