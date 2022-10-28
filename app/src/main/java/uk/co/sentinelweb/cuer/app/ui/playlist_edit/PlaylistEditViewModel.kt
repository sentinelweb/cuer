package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.DefaultFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.Flag.PLAY_START
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.UiEvent.Type.ERROR
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditViewModel.UiEvent.Type.MESSAGE
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract.Companion.ADD_PLAYLIST_DUMMY
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.PINNED_PLAYLIST
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.ext.buildLookup
import uk.co.sentinelweb.cuer.domain.ext.buildTree
import uk.co.sentinelweb.cuer.domain.ext.isAllWatched
import uk.co.sentinelweb.cuer.domain.ext.isAncestor


class PlaylistEditViewModel constructor(
    private val state: PlaylistEditContract.State,
    private val mapper: PlaylistEditModelMapper,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val log: LogWrapper,
    private val prefsWrapper: GeneralPreferencesWrapper,
    private val recentLocalPlaylists: RecentLocalPlaylists,
) : ViewModel() {
    init {
        log.tag(this)
    }

    data class UiEvent(
        val type: Type,
        val data: Any?
    ) {
        enum class Type { MESSAGE, ERROR, IMAGE }
    }

    private val _uiLiveData: MutableLiveData<UiEvent> = MutableLiveData()
    private val _modelLiveData: MutableLiveData<PlaylistEditContract.Model> = MutableLiveData()
    private val _domainLiveData: MutableLiveData<PlaylistDomain> = MutableLiveData()
    private val _dialogModelLiveData: MutableLiveData<DialogModel> = MutableLiveData()
    private val _navigateLiveData: MutableLiveData<NavigationModel> = MutableLiveData()

    fun getUiObservable(): LiveData<UiEvent> = _uiLiveData
    fun getModelObservable(): LiveData<PlaylistEditContract.Model> = _modelLiveData
    fun getDomainObservable(): LiveData<PlaylistDomain> = _domainLiveData
    fun getDialogObservable(): LiveData<DialogModel> = _dialogModelLiveData
    fun getNavigationObservable(): LiveData<NavigationModel> = _navigateLiveData

    @Suppress("RedundantOverride") // for note
    override fun onCleared() {
        super.onCleared()
        // https://developer.android.com/topic/libraries/architecture/coroutines
        // coroutines cancel via viewModelScope
    }

    fun setData(playlistId: Long?, source: Source) {
        viewModelScope.launch {
            if (!state.isLoaded) {
                state.source = source
                playlistId?.let {
                    playlistOrchestrator.load(it, source.deepOptions())
                        ?.also {
                            state.isAllWatched = it.isAllWatched()
                            state.defaultInitial = it.default
                            state.playlistEdit = it.copy(items = listOf())
                            it.parentId?.also {
                                state.playlistParent =
                                    playlistOrchestrator.load(it, LOCAL.flatOptions())
                            }
                        } ?: makeCreateModel()
                } ?: makeCreateModel()
                update()
                state.isLoaded = true
            }
        }
    }

    private fun update() {
        val pinned = prefsWrapper.getLong(PINNED_PLAYLIST, 0) == state.playlistEdit.id
        _modelLiveData.value = mapper.mapModel(
            domain = state.playlistEdit,
            pinned = pinned,
            parent = state.playlistParent,
            showAllWatched = state.isAllWatched == true,
            showDefault = !state.defaultInitial,
            isDialog = state.isDialog
        ).apply { state.model = this }
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
        // removed in 278
//        if (!state.isDialog) {
        _dialogModelLiveData.value = SearchImageContract.Config(
            state.playlistEdit.title,
            this::onImageSelected
        )
//        }
    }

    fun onImageSelected(image: ImageDomain) {
        state.playlistEdit = state.playlistEdit.copy(image = image, thumb = image)
        update()
        _dialogModelLiveData.value = DialogModel.DismissDialogModel()
    }

    fun onTitleChanged(text: String) {
        log.d("onTitleChanged($text)")
        if (state.playlistEdit.title != text) {
            state.playlistEdit = state.playlistEdit.copy(title = text)
            update()
        }
    }

    fun onCommitClick() {
        if ((state.model?.validation?.valid) == true) {
            viewModelScope.launch {
                if (state.playlistEdit.default && state.source == LOCAL) {
                    playlistOrchestrator.loadList(
                        DefaultFilter,
                        state.source.flatOptions()
                    )
                        .takeIf { it.size > 0 }
                        ?.map { it.copy(default = false) }
                        ?.apply {
                            playlistOrchestrator.save(this, state.source.flatOptions())
                        }
                }
                playlistOrchestrator.save(state.playlistEdit, state.source.flatOptions())
                    .also {
                        it.apply { state.playlistEdit = this }
                        _domainLiveData.value = it // this calls navigate back / listener in fragment
                        recentLocalPlaylists.addRecent(it)
                    }
                    .takeIf { state.isCreate }
                    ?.also { recentLocalPlaylists.addRecentId(it.id!!) }
            }
        }
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

    fun onWatchAllClick() = viewModelScope.launch {
        val watched = state.isAllWatched != false
        val newWatched = !watched
        log.d("watched load mem items = ${state.playlistEdit.id}")
        playlistOrchestrator.load(state.playlistEdit.id!!, state.source.deepOptions())
            ?.apply {
                mediaOrchestrator.save(
                    items.map { it.media.copy(watched = newWatched) },
                    state.source.deepOptions()
                )
                state.isAllWatched = newWatched
            }
        update()
    }


    enum class Flag { DEFAULT, PLAY_START, DELETABLE, EDITABLE, PLAYABLE, DELETE_ITEMS, EDIT_ITEMS }

    fun onFlagChanged(b: Boolean, f: Flag) {
        state.playlistEdit = when (f) {
            PLAY_START -> state.playlistEdit.copy(playItemsFromStart = b)
            Flag.DEFAULT -> state.playlistEdit.copy(default = b)
            Flag.DELETABLE -> state.playlistEdit.copy(
                config = state.playlistEdit.config.copy(
                    deletable = b
                )
            )
            Flag.EDITABLE -> state.playlistEdit.copy(
                config = state.playlistEdit.config.copy(
                    editable = b
                )
            )
            Flag.PLAYABLE -> state.playlistEdit.copy(
                config = state.playlistEdit.config.copy(
                    playable = b
                )
            )
            Flag.DELETE_ITEMS -> state.playlistEdit.copy(
                config = state.playlistEdit.config.copy(
                    deletableItems = b
                )
            )
            Flag.EDIT_ITEMS -> state.playlistEdit.copy(
                config = state.playlistEdit.config.copy(
                    editableItems = b
                )
            )
        }
        update()
    }

    fun onSelectParent() {
        _dialogModelLiveData.value =
            PlaylistsDialogContract.Config(
                setOf(),
                true,
                this::onParentSelected,
                { },
                this::onPlaylistDialogClose,
                null,
                showAdd = false,
                showPin = false,
                showRoot = true
            )
    }

    private fun onParentSelected(parent: PlaylistDomain?, checked: Boolean) = viewModelScope.launch {
        if (parent == ADD_PLAYLIST_DUMMY) {
            return@launch
        }
        if (state.treeLookup.isEmpty()) {
            state.treeLookup = playlistOrchestrator
                .loadList(AllFilter, LOCAL.flatOptions())
                .buildTree()
                .buildLookup()
        }
        state.playlistEdit.id
            ?.takeIf { state.source == LOCAL }
            ?.let { state.treeLookup[state.playlistEdit.id]!! }
            ?.also { childNode ->
                val parentNode = state.treeLookup[parent?.id]
                if (parent?.id == null || !childNode.isAncestor(parentNode!!)) {
                    setParent(parent)
                } else {
                    _uiLiveData.value = UiEvent(ERROR, "That's a circular reference ..")
                }
            }
            ?: also {
                setParent(parent)
            }
    }

    private fun setParent(parent: PlaylistDomain?) {
        state.playlistParent = parent?.id?.let { parent }
        state.playlistEdit = state.playlistEdit.copy(parentId = parent?.id)
        update()
    }

    fun onPlaylistDialogClose() {
        update()
    }

    fun onRemoveParent() {
        state.playlistParent = null
        state.playlistEdit = state.playlistEdit.copy(parentId = null)
        update()
    }

    fun onLinkClick(urlString: String) {
        _navigateLiveData.value =
            NavigationModel(
                NavigationModel.Target.WEB_LINK,
                mapOf(NavigationModel.Param.LINK to urlString)
            )
    }

    fun setIsDialog(b: Boolean) {
        state.isDialog = b
    }
}
