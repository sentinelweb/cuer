package uk.co.sentinelweb.cuer.app.ui.search.image

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
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

    data class UiEvent(
        val type: Type,
        val data: Any?
    ) {
        enum class Type { ERROR, LOADING }
    }

    private val _uiLiveData: MutableLiveData<UiEvent> = MutableLiveData()
    //private val _modelLiveData: MutableLiveData<Model> = MutableLiveData()

    fun getUiObservable(): LiveData<UiEvent> = _uiLiveData

    //fun getModelObservable(): LiveData<Model> = _modelLiveData
    var modelState: SearchImageContract.Model by mutableStateOf(mapper.map(state))
        private set
    var loadingState: Boolean by mutableStateOf(false)
        private set

    fun onSearchTextChange(text: String) {
        state.term = text
        //_modelLiveData.value = mapper.map(state)
    }

    fun onSearch() {
        viewModelScope.launch {
            //_uiLiveData.value = UiEvent(UiEvent.Type.LOADING, true)
            loadingState = true
            state.term?.let {
                pixabayInteractor.images(it)
                    .takeIf { it.isSuccessful }
                    ?.data
                    ?.also { state.images = it }
                    //?.also { _modelLiveData.value = mapper.map(state) }
                    ?.also { modelState = mapper.map(state) }
                    .also {
//                        _uiLiveData.value = UiEvent(UiEvent.Type.LOADING, false)
                        loadingState = false
                    }
                    ?: let {
                        showError("Search Failed")
                        //_uiLiveData.value = UiEvent(UiEvent.Type.LOADING, false)
                        loadingState = false
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
        _uiLiveData.value = UiEvent(UiEvent.Type.ERROR, msg)
    }

    fun onImageSelected(image: ImageDomain) {
        state.config?.let { it.itemClick(image) }
    }

    fun setConfig(config: SearchImageContract.Config) {
        state.config = config
        state.term = config.initialTerm
        onSearch()
    }

}