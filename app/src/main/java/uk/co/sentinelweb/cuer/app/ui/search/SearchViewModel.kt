package uk.co.sentinelweb.cuer.app.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class SearchViewModel(
    private state: SearchContract.State,
    private val log: LogWrapper
) : ViewModel() {

    var searchState: SearchContract.State by mutableStateOf(state)
        private set

    fun onSearchTextChange(text: String) {
        searchState = searchState.copy(text = text)
    }

    fun onSubmit() {
        log.d("Execute search ... ${searchState.text}")
    }
}