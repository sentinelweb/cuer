package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Options
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_PLAYLIST_CREATED
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain


class PlaylistEditViewModel constructor(
    private val state: PlaylistEditContract.State,
    private val mapper: PlaylistEditModelMapper,
    private val playlistRepo: PlaylistOrchestrator,
    private val log: LogWrapper,
    private val imageProvider: FirebaseDefaultImageProvider,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>
) : ViewModel() {

    private val _modelLiveData: MutableLiveData<PlaylistEditContract.Model> = MutableLiveData()
    private val _domainLiveData: MutableLiveData<PlaylistDomain> = MutableLiveData()

    fun getModelObservable(): LiveData<PlaylistEditContract.Model> = _modelLiveData
    fun getDomainObservable(): LiveData<PlaylistDomain> = _domainLiveData

    @Suppress("RedundantOverride") // for note
    override fun onCleared() {
        super.onCleared()
        // https://developer.android.com/topic/libraries/architecture/coroutines
        // coroutines cancel via viewModelScope
    }

    fun setData(playlistId: Long?, source: Source) {
        viewModelScope.launch {
            state.source = source
            playlistId?.let {
                playlistRepo.load(it, Options(source))
                    ?.let {
                        state.playlist = it
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

    fun onImageClick(forward: Boolean) {
        imageProvider.getNextImage(state.playlist.image, forward) { next ->
            if (next != null) {
                state.playlist = state.playlist.copy(image = next)
                update()
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
                playlistRepo.save(state.playlist, Options(state.source))
                    .also {
                        it.apply { state.playlist = this }
                        _domainLiveData.value = it
                    }.takeIf { state.isCreate }
                    ?.also {
                        prefsWrapper.putLong(LAST_PLAYLIST_CREATED, it.id!!)
                    }
            }
        }
    }

    private fun update() {
        state.model = mapper.mapModel(state.playlist)
        _modelLiveData.value = mapper.mapModel(state.playlist)
    }

}
