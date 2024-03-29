package uk.co.sentinelweb.cuer.app.ui.search.image

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.file.ImageFileRepository
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageViewModel.UiEvent.Type.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.net.pixabay.PixabayInteractor

class SearchImageViewModel(
    private val state: SearchImageContract.State,
    private val mapper: SearchImageContract.Mapper,
    private val log: LogWrapper,
    private val pixabayInteractor: PixabayInteractor,
    private val imageFileRepository: ImageFileRepository,
) : ViewModel() {

    init {
        log.tag(this)
    }

    data class UiEvent(
        val type: Type,
        val data: Any?
    ) {
        enum class Type { ERROR, CLOSE, GOTO_LIBRARY }
    }

    private val _uiLiveData: MutableLiveData<UiEvent> = MutableLiveData()

    fun getUiObservable(): LiveData<UiEvent> = _uiLiveData

    var searchState: SearchImageContract.SearchModel by mutableStateOf(mapper.mapSearch(state))
        private set
    var resultsState: SearchImageContract.ResultsModel by mutableStateOf(mapper.mapResults(state))
        private set

    fun onSearchTextChange(text: String) {
        state.term = text
        searchState = mapper.mapSearch(state)
    }

    fun onSearch() {
        viewModelScope.launch {
            state.loading = true
            searchState = mapper.mapSearch(state)
            state.term?.let {
                pixabayInteractor.images(it)
                    .takeIf { it.isSuccessful }
                    ?.data
                    ?.also { state.images = it }
                    ?.also { resultsState = mapper.mapResults(state) }
                    .also {
                        state.loading = false
                        searchState = mapper.mapSearch(state)
                    }
                    ?: let {
                        showError("Search Failed")
                        state.loading = false
                        searchState = mapper.mapSearch(state)
                        null
                    }
            } ?: let {
                showError("Please enter a search term")
                null
            }
        }
    }

    private fun showError(msg: String) {
        log.e(msg)
        _uiLiveData.value = UiEvent(ERROR, msg)
    }

    fun onImageSelected(image: ImageDomain) = viewModelScope.launch {
        state.loading = true
        searchState = mapper.mapSearch(state)
        val nameBase =
            if (!state.term.isNullOrBlank() && !image.url.startsWith("file")) state.term?.trim()
            else null
        val savedImage = imageFileRepository.saveImage(image, nameBase)
        log.d("saved image: $savedImage")
        state.config?.let { it.itemClick(savedImage) }
        state.loading = false
        searchState = mapper.mapSearch(state)
        _uiLiveData.value = UiEvent(CLOSE, null)
    }

    fun onLibraryClick() {
        _uiLiveData.value = UiEvent(GOTO_LIBRARY, null)
    }

    fun onClose() {
        _uiLiveData.value = UiEvent(CLOSE, null)
    }

    fun setConfig(config: SearchImageContract.Config) {
        state.config = config
        state.term = config.initialTerm
        onSearch()
    }

}