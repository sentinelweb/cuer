package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.UiEvent.Type.MESSAGE
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_PLAYLIST_CREATED
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.PINNED_PLAYLIST
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.ext.isAllWatched


class PlaylistEditViewModel constructor(
    private val state: PlaylistEditContract.State,
    private val mapper: PlaylistEditModelMapper,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val log: LogWrapper,
    private val imageProvider: FirebaseDefaultImageProvider,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>
) : ViewModel() {

    data class UiEvent(
        val type: Type,
        val data: Any?
    ) {
        enum class Type { MESSAGE, ERROR }
    }

    private val _uiLiveData: MutableLiveData<UiEvent> = MutableLiveData()
    private val _modelLiveData: MutableLiveData<PlaylistEditContract.Model> = MutableLiveData()
    private val _domainLiveData: MutableLiveData<PlaylistDomain> = MutableLiveData()

    fun getUiObservable(): LiveData<PlaylistEditViewModel.UiEvent> = _uiLiveData
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
                playlistOrchestrator.load(it, source.deepOptions())
                    ?.let {
                        // state.playlist = it
                        state.isAllWatched = it.isAllWatched()
                        state.playlistEdit = it.copy(items = listOf())
                    } ?: makeCreateModel()
            } ?: makeCreateModel()
            update()
        }
    }

    fun onStarClick() {
        state.playlistEdit = state.playlistEdit.copy(starred = !state.playlistEdit.starred)
        update()
    }

    private fun makeCreateModel() {
        state.isCreate = true
        state.playlistEdit = PlaylistDomain(
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
        imageProvider.getNextImage(state.playlistEdit.image, forward) { next ->
            if (next != null) {
                state.playlistEdit = state.playlistEdit.copy(image = next, thumb = next)
                update()
            }
        }
    }

    fun onTitleChanged(text: String) {
        log.d("onTitleChanged($text)")
        if (state.playlistEdit.title != text) {
            state.playlistEdit = state.playlistEdit.copy(title = text)
            update()
        }
    }

    fun onCommitClick() {
        if (state.model?.validation?.valid ?: false) {
            viewModelScope.launch {
                if (state.playlistEdit.default && state.source == Source.LOCAL) {
                    playlistOrchestrator.loadList(OrchestratorContract.DefaultFilter(), state.source.flatOptions())
                        .takeIf { it.size > 0 }
                        ?.map { it.copy(default = false) }
                        ?.apply {
                            playlistOrchestrator.save(this, state.source.flatOptions())
                        }
                }
                playlistOrchestrator.save(state.playlistEdit, state.source.flatOptions())
                    .also {
                        it.apply { state.playlistEdit = this }
                        _domainLiveData.value = it
                    }.takeIf { state.isCreate }
                    ?.also {
                        prefsWrapper.putLong(LAST_PLAYLIST_CREATED, it.id!!)
                    }
            }
        }
    }

    private fun update() {
        val pinned = prefsWrapper.getLong(PINNED_PLAYLIST, 0) == state.playlistEdit.id
        _modelLiveData.value = mapper.mapModel(state.playlistEdit, pinned, showAllWatched = state.isAllWatched == true)
            .apply { state.model = this }
    }

    fun onPinClick() {
        state.playlistEdit.id?.apply {
            val pinnedId = prefsWrapper.getLong(PINNED_PLAYLIST, 0)
            if (pinnedId != state.playlistEdit.id) {
                prefsWrapper.putLong(PINNED_PLAYLIST, this)
            } else {
                prefsWrapper.remove(PINNED_PLAYLIST)
            }
            update()
        } ?: run {
            _uiLiveData.value = UiEvent(MESSAGE, "Please save the playlist first")
        }
    }

    fun onWatchAllClick() {
        viewModelScope.launch {
            playlistOrchestrator.load(state.playlistEdit.id!!, state.source.deepOptions())
                ?.apply {
                    val watched = state.isAllWatched == true
                    mediaOrchestrator.save(items.map { it.media.copy(watched = !watched) }, state.source.deepOptions())
                    state.isAllWatched = !watched
                    update()
                }
        }
    }

    fun onPlayStartChanged(b: Boolean) {
        state.playlistEdit = state.playlistEdit.copy(playItemsFromStart = b)
        update()
    }

    fun onDefaultChanged(b: Boolean) {
        state.playlistEdit = state.playlistEdit.copy(default = b)
        update()
    }

    fun onSelectParent() {
        log.d("onSelectParent")
    }

    fun onRemoveParent() {
        log.d("onRemoveParent")
    }


}
