package uk.co.sentinelweb.cuer.app.ui.search.image

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageContract.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.net.pixabay.PixabayInteractor

class SearchImageViewModel(
    private val state: SearchImageContract.State,
    private val mapper: SearchImageContract.Mapper,
    private val log: LogWrapper,
    private val pixabayInteractor: PixabayInteractor
) : ViewModel() {

    init {
        log.tag(this)
    }

    var model: Model by mutableStateOf(mapper.map(state))
        private set

    fun onSearchTextChange(text: String) {
        state.term = text
        model = mapper.map(state)
    }

    fun onSearch() {
        viewModelScope.launch {
            state.term?.let {
                pixabayInteractor.images(it)
                    .takeIf { it.isSuccessful }
                    ?.data
                    ?.also { state.images = it }
                    ?.also { model = mapper.map(state) }
                    ?: let {
                        showError("Search Failed")
                        null
                    }
            } ?: let {
                showError("Please enter a search term")
                null
            }
        }
    }

    private fun showError(msg: String) {
        state.message = msg
        log.e(msg)
        model = mapper.map(state)
    }

    fun onImageSelected(index: Int) {
        state.images
            ?.takeIf { index < (state.images?.size ?: 0) }
            ?.get(index)
            ?.apply {
                state.config?.let { it.itemClick(this) }
            }
            ?: showError("Could get iamge")
    }

    fun setConfig(config: SearchImageContract.Config) {
        state.config = config
        onSearch()
    }

}