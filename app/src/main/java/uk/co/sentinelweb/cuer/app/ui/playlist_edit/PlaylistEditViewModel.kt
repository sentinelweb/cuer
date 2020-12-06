package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain


class PlaylistEditViewModel constructor(
    private val state: PlaylistEditState,
    private val mapper: PlaylistEditModelMapper,
    private val playlistRepo: PlaylistDatabaseRepository,
    private val log: LogWrapper,
    private val imageProvider: FirebaseDefaultImageProvider
) : ViewModel() {

    private val _modelLiveData: MutableLiveData<PlaylistEditModel> = MutableLiveData()
    private val _domainLiveData: MutableLiveData<PlaylistDomain> = MutableLiveData()

    fun getModelObservable(): LiveData<PlaylistEditModel> = _modelLiveData
    fun getDomainObservable(): LiveData<PlaylistDomain> = _domainLiveData

    @Suppress("RedundantOverride") // for note
    override fun onCleared() {
        super.onCleared()
        // https://developer.android.com/topic/libraries/architecture/coroutines
        // coroutines cancel via viewModelScope
    }

    fun setData(playlistId: Long?) {
        viewModelScope.launch {
            playlistId?.let {
                playlistRepo.load(it, true)
                    .takeIf { it.isSuccessful }
                    ?.let {
                        it.data?.let {
                            state.playlist = it
                        } ?: makeCreateModel()
                    } ?: makeCreateModel()
            } ?: makeCreateModel()
            update()
        }
    }

    fun onStarClick() {
        state.playlist = state.playlist.copy(starred = !state.playlist.starred)
        update()
    }

    private fun makeCreateModel() {
        state.isCreate = true
        state.playlist = PlaylistDomain(
            title = "",
            image = ImageDomain(
                url = "gs://cuer-275020.appspot.com/playlist_header/pexels-freestocksorg-34407-600.jpg",
                width = null,
                height = null
            ),
            items = listOf()
        )
    }

    fun onImageClick() {
        state.playlist.image?.let {
            imageProvider.getNextImage(it) { next ->
                if (next != null) {
                    state.playlist = state.playlist.copy(image = next)
                    update()
                }
            }
        }
    }

    fun onTitleChanged(text: String) {
        log.d("onTitleChanged($text)")
        if (state.playlist.title != text) {
            state.playlist = state.playlist.copy(title = text)
            update()
        }
    }

    fun onCommitClick() {
        if (state.model?.validation?.valid ?: false) {
            viewModelScope.launch {
                playlistRepo.save(state.playlist, true)
                    .takeIf { it.isSuccessful }
                    ?.let {
                        it.data?.apply { state.playlist = this }
                        _domainLiveData.value = it.data
                    }
            }
        }
    }

    private fun update() {
        state.model = mapper.mapModel(state.playlist)
        _modelLiveData.value = mapper.mapModel(state.playlist)
    }

}
