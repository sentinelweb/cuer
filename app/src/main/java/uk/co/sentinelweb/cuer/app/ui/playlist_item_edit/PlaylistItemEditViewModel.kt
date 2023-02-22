package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.exception.NoDefaultPlaylistException
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Options
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.ArgumentDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel.Type.PLAYLIST_ADD
import uk.co.sentinelweb.cuer.app.ui.common.navigation.LinkNavigator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.SOURCE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonModel
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditViewModel.UiEvent.Type.*
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract.Companion.ADD_PLAYLIST_DUMMY
import uk.co.sentinelweb.cuer.app.ui.share.ShareCommitter
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.domain.ext.deserialiseGuidIdentifier
import uk.co.sentinelweb.cuer.domain.ext.domainJsonSerializer
import uk.co.sentinelweb.cuer.domain.mappers.PlaylistAndItemMapper


class PlaylistItemEditViewModel constructor(
    private val state: PlaylistItemEditContract.State,
    private val modelMapper: PlaylistItemEditModelMapper,
    private val itemCreator: PlaylistItemCreator,
    private val log: LogWrapper,
    private val toast: ToastWrapper,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val shareWrapper: AndroidShareWrapper,
    private val playUseCase: PlayUseCase,
    private val linkNavigator: LinkNavigator,
    private val recentLocalPlaylists: RecentLocalPlaylists,
    private val res: ResourceWrapper,
    private val coroutines: CoroutineContextProvider,
    private val timeProvider: TimeProvider,
    private val paiMapper: PlaylistAndItemMapper,
) : ViewModel(), DescriptionContract.Interactions {
    init {
        log.tag(this)
    }

    data class UiEvent(
        val type: Type,
        val data: Any?,
    ) {
        enum class Type { REFRESHING, ERROR, UNPIN, JUMPTO }
    }

    private val _uiLiveData: MutableLiveData<UiEvent> = MutableLiveData()
    private val _modelLiveData: MutableLiveData<PlaylistItemEditContract.Model> = MutableLiveData()
    private val _dialogModelLiveData: MutableLiveData<DialogModel> = MutableLiveData()
    private val _navigateLiveData: MutableLiveData<NavigationModel> = MutableLiveData()

    fun getUiObservable(): LiveData<UiEvent> = _uiLiveData
    fun getModelObservable(): LiveData<PlaylistItemEditContract.Model> = _modelLiveData
    fun getNavigationObservable(): LiveData<NavigationModel> = _navigateLiveData
    fun getDialogObservable(): LiveData<DialogModel> = _dialogModelLiveData

    private val isNew: Boolean
        get() = state.editingPlaylistItem?.id == null

    fun setInShare(isInShare: Boolean) {
        state.isInShare = isInShare
    }

    fun onResume() {
        listen()
    }

    private fun listen() {
        mediaOrchestrator.updates
            .onEach { (op, source, newMedia) ->
                //log.d("media changed: $op, $source, id=${newMedia.id} title=${newMedia.title}")
                when (op) {
                    FLAT, FULL -> {
                        if (newMedia.platformId == state.media?.platformId) {
                            state.media = newMedia.copy(
                                starred = state.media?.starred ?: newMedia.starred,
                                watched = state.media?.watched ?: newMedia.watched,
                                playFromStart = state.media?.playFromStart ?: newMedia.playFromStart,
                            )
                            update()
                        }
                    }

                    OrchestratorContract.Operation.DELETE -> Unit
                }
            }
            .launchIn(coroutines.mainScope)
    }

    fun setData(
        item: PlaylistItemDomain,
        source: Source,
        parentId: GUID?,
        allowPlay: Boolean,
        isOnSharePlaylist: Boolean
    ) {
        item.let {
            state.editingPlaylistItem = it
            state.source = source
            state.parentPlaylistId = parentId?.toIdentifier(source)
            state.allowPlay = allowPlay
            state.isOnSharePlaylist = isOnSharePlaylist
            it.media.let { setData(it) }
        }
    }

    private fun setData(media: MediaDomain?) {
        viewModelScope.launch {
            state.isMediaChanged = media?.id == null
            media?.let { originalMedia ->
                state.media = originalMedia
                originalMedia.id?.let {
                    playlistItemOrchestrator.loadList(MediaIdListFilter(listOf(it.id)), Options(state.source))
                        .takeIf { it.size > 0 }
                        ?.also { if (isNew) state.editingPlaylistItem = it[0] }
                        ?.also {
                            it.map { it.playlistId }
                                .distinct()
                                .filterNotNull()
                                .also {
                                    playlistOrchestrator.loadList(IdListFilter(it.map { it.id }), state.source.flatOptions())
                                        .also { state.selectedPlaylists.addAll(it) }
                                }
                        }
                }

                state.parentPlaylistId
                    ?.apply {
                        playlistOrchestrator.loadById(this.id, LOCAL.flatOptions())
                            ?.also { state.selectedPlaylists.add(it) }
                    }

                prefsWrapper.pinnedPlaylistId
                    ?.takeIf { state.selectedPlaylists.size == 0 }
                    ?.let { playlistOrchestrator.loadById(it, LOCAL.flatOptions()) }
                    ?.also { state.selectedPlaylists.add(it) }

                if (originalMedia.channelData.thumbNail == null
                    || (originalMedia.duration?.let { it > 1000 * 60 * 60 * 24 } ?: true)
                    || originalMedia.isLiveBroadcast
                    || originalMedia.isLiveBroadcastUpcoming
                ) {
                    refreshMedia()
                } else {
                    checkToAutoSelectPlaylists()
                    update()
                }
            } ?: run {
                _modelLiveData.value = modelMapper.mapEmpty()
            }
        }
    }

    private suspend fun checkToAutoSelectPlaylists() {
        if (state.selectedPlaylists.isEmpty() && !state.isOnSharePlaylist) {
            state.media?.apply {
                val channelFilter = ChannelPlatformIdFilter(channelData.platformId!!)
                playlistOrchestrator.loadList(
                    channelFilter,
                    LOCAL.flatOptions()
                ).also {
                    if (it.size == 1) {
                        state.selectedPlaylists.add(it[0])
                    } else if (it.size > 1) {
                        playlistItemOrchestrator
                            .loadList(channelFilter, LOCAL.flatOptions())
                            .groupBy { it.playlistId }
                            .values
                            .maxByOrNull { it.size } // should get the largest list of items
                            ?.get(0)
                            ?.playlistId
                            ?.let { playlistOrchestrator.loadById(it.id, LOCAL.flatOptions()) }
                            ?.also { state.selectedPlaylists.add(it) }
                    }
                }
            }
        }
    }

    fun refreshMediaBackground() = viewModelScope.launch {
        refreshMedia()
    }

    override fun onPlaylistChipClick(chipModel: ChipModel) {
        if (!state.isInShare) {
            val plId = deserialiseGuidIdentifier(chipModel.value!!)
            _navigateLiveData.value = NavigationModel(PLAYLIST, mapOf(PLAYLIST_ID to plId.id.value, SOURCE to plId.source))
            _navigateLiveData.value = NavigationModel(NAV_NONE)
        }
    }

    private suspend fun refreshMedia() =
        withContext(viewModelScope.coroutineContext) {
            state.media?.let { originalMedia ->
                _uiLiveData.value = UiEvent(REFRESHING, true)
                try {
                    mediaOrchestrator.loadByPlatformId(originalMedia.platformId, Options(PLATFORM))
                        ?.let {
                            it.copy(
                                id = originalMedia.id,
                                dateLastPlayed = originalMedia.dateLastPlayed,
                                starred = originalMedia.starred,
                                watched = originalMedia.watched,
                            )
                                .also { state.media = it }
                                .also { state.isMediaChanged = true }
                                .also { checkToAutoSelectPlaylists() }
                                .also { update() }
                        }
                        ?: also { updateError() }
                } catch (e: Exception) {
                    log.e("Caught Exception updating media", e)
                    _uiLiveData.value = UiEvent(ERROR, "Error ${e.message}")
                } finally {
                    _uiLiveData.value = UiEvent(REFRESHING, false)
                }
            }
        }

    fun onPlayVideo() {
        state.editingPlaylistItem?.let { item ->
            playUseCase.playLogic(paiMapper.map(state.selectedPlaylists.firstOrNull(), item), false)
        } ?: run { toast.show("Please save the item first ...") }
    }

    private fun onStarClick() {
        state.media
            ?.let { it.copy(starred = !it.starred) }
            ?.also { state.media = it }
            ?.also { state.isMediaChanged = true }
            ?.also { update() }
    }

    override fun onSelectPlaylistChipClick(model: ChipModel) {
        _dialogModelLiveData.value =
            PlaylistsMviDialogContract.Config(
                res.getString(R.string.playlist_dialog_title),
                state.selectedPlaylists,
                true,
                this@PlaylistItemEditViewModel::onPlaylistSelected,
                { },
                this@PlaylistItemEditViewModel::onPlaylistDialogClose,
                state.media,
                showAdd = true,
                showPin = true
            )
    }

    fun onPlaylistSelected(playlist: PlaylistDomain?, checked: Boolean) {
        state.isPlaylistsChanged = true
        playlist
            ?.takeIf { playlist != ADD_PLAYLIST_DUMMY }
            ?.apply {
                if (checked) {
                    state.selectedPlaylists.add(this)
                    state.deletedPlayLists.removeIf { it.id == this.id }
                } else {
                    state.selectedPlaylists.remove(this)
                    state.deletedPlayLists.add(this)
                }
            }
            ?.also { update() }
            ?: apply {
                _dialogModelLiveData.value =
                    DialogModel(PLAYLIST_ADD, res.getString(R.string.create_playlist_dialog_title))
            }
    }

    fun onUnPin() {
        prefsWrapper.pinnedPlaylistId = null
    }

    fun onPlaylistCreated(domain: PlaylistDomain) {
        viewModelScope.launch {
            state.isPlaylistsChanged = true
            state.selectedPlaylists.add(domain)
            update()
        }
    }

    fun onPlaylistDialogClose() {
        update()
    }

    private fun onEditClick() {
        state.media?.let { originalMedia ->
            state.editSettings.watched = null
            state.editSettings.playFromStart = null
            _dialogModelLiveData.value = modelMapper.mapItemSettings(
                originalMedia,
                { i, selected ->
                    when (i) {
                        0 -> state.editSettings.watched = selected
                        1 -> state.editSettings.playFromStart = selected
                    }
                },
                {
                    state.media = state.media?.copy(
                        watched = state.editSettings.watched
                            ?: originalMedia.watched,
                        positon = state.editSettings.watched
                            ?.takeIf { it.not() }
                            ?.let { 0 }
                            ?: originalMedia.positon,
                        playFromStart = state.editSettings.playFromStart
                            ?: originalMedia.playFromStart
                    )
                    state.isMediaChanged = true
                }
            )
        }
    }

    override fun onRemovePlaylist(chipModel: ChipModel) {
        state.isPlaylistsChanged = true
        val plId = deserialiseGuidIdentifier(chipModel.value!!)
        state.selectedPlaylists
            .find { it.id == plId }
            ?.also {
                state.selectedPlaylists.remove(it)
                state.deletedPlayLists.add(it)
            }
        prefsWrapper.pinnedPlaylistId
            ?.takeIf { it == plId.id }
            ?.apply {
                _uiLiveData.value = UiEvent(UNPIN, null)
            }
        update()
    }

    override fun onRibbonItemClick(ribbonItem: RibbonModel) = when (ribbonItem.type) {
        RibbonModel.Type.LIKE -> onLaunchVideo()
        RibbonModel.Type.STAR -> onStarClick()
        RibbonModel.Type.UNSTAR -> onStarClick()
        RibbonModel.Type.SHARE -> onShare()
        RibbonModel.Type.SUPPORT -> onSupport()
        RibbonModel.Type.COMMENT -> onLaunchVideo()
        RibbonModel.Type.LAUNCH -> onLaunchVideo()
        RibbonModel.Type.EDIT -> onEditClick()
        else -> log.e("Unsupported ribbon action", IllegalStateException("Unsupported ribbon action: $ribbonItem"))
    }

    override fun onChannelClick() {
        state.media?.channelData?.platformId?.let { channelId ->
            _navigateLiveData.value = NavigationModel(
                YOUTUBE_CHANNEL,
                mapOf(NavigationModel.Param.CHANNEL_ID to channelId)
            )
        }
    }

    override fun onLinkClick(link: LinkDomain.UrlLinkDomain) {
        linkNavigator.navigateLink(link)
    }

    override fun onCryptoClick(cryptoAddress: LinkDomain.CryptoLinkDomain) {
        _navigateLiveData.value =
            NavigationModel(CRYPTO_LINK, mapOf(NavigationModel.Param.CRYPTO_ADDRESS to cryptoAddress))
    }

    override fun onTimecodeClick(timecode: TimecodeDomain) {
        _uiLiveData.value = UiEvent(JUMPTO, timecode.position)
    }

    private fun onLaunchVideo() {
        state.editingPlaylistItem?.let {
            _navigateLiveData.value =
                NavigationModel(YOUTUBE_VIDEO_POS, mapOf(NavigationModel.Param.PLAYLIST_ITEM to it))
        }
    }

    private fun onShare() {
        state.media
            ?.apply { shareWrapper.share(this) }
            ?: toast.show("No item to share ...")
    }

    private fun update() = modelMapper.map(state).apply {
        state.model = this
        _modelLiveData.value = this
    }

    private fun updateError() = modelMapper.mapEmpty().apply {
        state.model = this
        _modelLiveData.value = this
    }

    fun checkToSave() {
        if (!state.isSaved && (state.isMediaChanged || state.isPlaylistsChanged)) {
            if (state.media?.id?.source == MEMORY) { // share playlist media ids are null - this is for youtube search (created ids to play)
                _navigateLiveData.value = NavigationModel(NAV_DONE)
            } else if (state.isOnSharePlaylist) {
                doCommitAndReturn()
            } else if (isNew) {
                _dialogModelLiveData.value = modelMapper.mapSaveConfirmAlert({
                    doCommitAndReturn()
                }, { _navigateLiveData.value = NavigationModel(NAV_DONE) })
            } else {
                if (state.selectedPlaylists.size > 0) {
                    doCommitAndReturn()
                } else {
                    _uiLiveData.value =
                        UiEvent(ERROR, "Please select a playlist (or delete the item)")
                }
            }
        } else {
            _navigateLiveData.value = NavigationModel(NAV_DONE)
        }
    }

    private fun doCommitAndReturn() {
        viewModelScope.launch {
            commitPlaylistItems()
            _navigateLiveData.value = NavigationModel(NAV_DONE)
        }
    }

    suspend fun commitPlaylistItems(afterCommit: ShareCommitter.AfterCommit? = null) =
        try {
            val selectedPlaylists = if (state.selectedPlaylists.size > 0) {
                state.selectedPlaylists
            } else {
                playlistOrchestrator.loadList(DefaultFilter, LOCAL.flatOptions())
                    .takeIf { it.size > 0 }
                    ?: throw NoDefaultPlaylistException()
            }
            val saveSource = if (isNew) LOCAL else state.source
            if (state.isPlaylistsChanged && state.editingPlaylistItem?.playlistId != null && state.deletedPlayLists.size > 0) {
                val deletePlaylistIds = state.deletedPlayLists.map { it.id }
                state.editingPlaylistItem?.media?.platformId?.let {
                    playlistItemOrchestrator.loadList(
                        PlatformIdListFilter(listOf(it), PlatformDomain.YOUTUBE),
                        state.source.flatOptions(false)
                    )
                }?.forEach { item ->
                    if (item.playlistId in deletePlaylistIds) {
                        playlistItemOrchestrator.delete(item, Options(state.source))
                    }
                }
            }
            log.d("commit items: ${state.isMediaChanged}")
            state.committedItems = (
                    state.media
                        ?.let {
                            if (state.isMediaChanged) {
                                if (state.isOnSharePlaylist) {
                                    mediaOrchestrator.save(it, state.source.flatOptions())
                                } else {
                                    mediaOrchestrator.save(it, saveSource.deepOptions())
                                        .also { log.d("after mediaOrchestrator.save") }

                                }
                            } else it
                        }
                        ?.let { savedMedia ->
                            state.media = savedMedia
                            val existingPlaylistItems = playlistItemOrchestrator
                                .loadList(PlatformIdListFilter(listOf(savedMedia.platformId)), LOCAL.flatOptions())
                            selectedPlaylists
                                .filter { playlist -> existingPlaylistItems.find { it.playlistId == playlist.id } == null }
                                .mapNotNull { playlist ->
                                    val domain = itemCreator.buildPlayListItem(
                                        savedMedia,
                                        playlist,
                                        dateAdded = timeProvider.instant()
                                    )
                                    playlistItemOrchestrator.save(
                                        domain,
                                        saveSource.flatOptions()
                                    )
                                        .takeIf { saveSource == LOCAL && isNew }
                                        ?.also {
                                            prefsWrapper.lastAddedPlaylistId = it.playlistId!!.id
                                            recentLocalPlaylists.addRecentId(it.playlistId!!.id)
                                        }
                                }
                        }
                        ?: listOf()
                    )
                .also { afterCommit?.onCommit(ObjectTypeDomain.PLAYLIST_ITEM, it) }
            state.isSaved = true
        } catch (e: Exception) {
            log.e("Error saving playlistItem", e)
            throw IllegalStateException("Save failed", e)
        }

    fun serializeState(): String =
        domainJsonSerializer.encodeToString(PlaylistItemEditContract.State.serializer(), state)

    fun restoreState(s: String) {
        domainJsonSerializer.decodeFromString(PlaylistItemEditContract.State.serializer(), s)
            .also { restored ->
                state.apply {
                    media = restored.media
                    selectedPlaylists.clear()
                    selectedPlaylists.addAll(restored.selectedPlaylists)
                    committedItems = restored.committedItems
                    editingPlaylistItem = restored.editingPlaylistItem
                    isPlaylistsChanged = restored.isPlaylistsChanged
                    isMediaChanged = restored.isMediaChanged
                    isSaved = restored.isSaved
                    editSettings.playFromStart = restored.editSettings.playFromStart
                    editSettings.watched = restored.editSettings.watched
                    parentPlaylistId = restored.parentPlaylistId
                    model = modelMapper.map(state)
                }
            }
    }

    private fun onSupport() {
        state.media
            ?.also { media ->
                _dialogModelLiveData.value = ArgumentDialogModel(
                    DialogModel.Type.SUPPORT,
                    res.getString(R.string.menu_support),
                    mapOf(NavigationModel.Param.MEDIA.toString() to media)
                )
            }
    }
}
