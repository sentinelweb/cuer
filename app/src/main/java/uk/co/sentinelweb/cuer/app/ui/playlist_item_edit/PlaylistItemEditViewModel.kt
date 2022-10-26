package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.exception.NoDefaultPlaylistException
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.PLATFORM
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.ArgumentDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel.Type.PLAYLIST_ADD
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.SOURCE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonModel
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditViewModel.UiEvent.Type.*
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract.Companion.ADD_PLAYLIST_DUMMY
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_PLAYLIST_ADDED_TO
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.domain.ext.domainJsonSerializer


class PlaylistItemEditViewModel constructor(
    private val state: PlaylistItemEditContract.State,
    private val modelMapper: PlaylistItemEditModelMapper,
    private val itemCreator: PlaylistItemCreator,
    private val log: LogWrapper,
    private val toast: ToastWrapper,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val mediaOrchestrator: MediaOrchestrator,
    private val prefsWrapper: GeneralPreferencesWrapper,
    private val shareWrapper: ShareWrapper,
    private val playUseCase: PlayUseCase,
    private val linkScanner: LinkScanner,
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

    fun setData(
        item: PlaylistItemDomain,
        source: Source,
        parentId: Long?,
        allowPlay: Boolean,
        isOnSharePlaylist: Boolean
    ) {
        item.let {
            state.editingPlaylistItem = it
            state.source = source
            state.parentPlaylistId = parentId ?: -1
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
                    playlistItemOrchestrator.loadList(
                        MediaIdListFilter(listOf(it)),
                        Options(state.source)
                    )
                        .takeIf { it.size > 0 }
                        ?.also { if (isNew) state.editingPlaylistItem = it[0] }
                        ?.also {
                            it.map { it.playlistId }
                                .distinct()
                                .filterNotNull()
                                .also {
                                    playlistOrchestrator.loadList(
                                        IdListFilter(it),
                                        state.source.flatOptions()
                                    )
                                        .also { state.selectedPlaylists.addAll(it) }
                                }
                        }
                }

                if (state.parentPlaylistId > 0L) {
                    playlistOrchestrator.load(state.parentPlaylistId, LOCAL.flatOptions())
                        ?.also { state.selectedPlaylists.add(it) }
                }

                prefsWrapper.getLong(GeneralPreferences.PINNED_PLAYLIST)
                    ?.takeIf { state.selectedPlaylists.size == 0 }
                    ?.let { playlistOrchestrator.load(it, LOCAL.flatOptions()) }
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
                            ?.let { playlistOrchestrator.load(it, LOCAL.flatOptions()) }
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
            val plId = chipModel.value?.toLong()
            _navigateLiveData.value = NavigationModel(PLAYLIST, mapOf(PLAYLIST_ID to plId, SOURCE to LOCAL))
            _navigateLiveData.value = NavigationModel(NAV_NONE)
        }
    }

    private suspend fun refreshMedia() =
        withContext(viewModelScope.coroutineContext) {
            state.media?.let { originalMedia ->
                _uiLiveData.value = UiEvent(REFRESHING, true)
                try {
                    mediaOrchestrator.load(originalMedia.platformId, Options(PLATFORM))
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
            playUseCase.playLogic(item, state.selectedPlaylists.firstOrNull(), false)
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
            PlaylistsDialogContract.Config(
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
                    DialogModel(PLAYLIST_ADD, R.string.create_playlist_dialog_title)
            }
    }

    fun onUnPin() {
        prefsWrapper.remove(GeneralPreferences.PINNED_PLAYLIST)
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
                        watched = state.editSettings.watched ?: originalMedia.watched,
                        positon = state.editSettings.watched?.takeIf { it.not() }?.let { 0 }
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
        val plId = chipModel.value?.toLong()
        state.selectedPlaylists
            .find { it.id == plId }
            ?.also {
                state.selectedPlaylists.remove(it)
                state.deletedPlayLists.add(it)
            }
        prefsWrapper.getLong(GeneralPreferences.PINNED_PLAYLIST)
            ?.takeIf { it == plId }
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
        _navigateLiveData.value = linkScanner.scan(link)?.let { scanned ->
            when (scanned.first) {
                ObjectTypeDomain.MEDIA -> navShare(link)
                ObjectTypeDomain.PLAYLIST -> navShare(link)
                ObjectTypeDomain.PLAYLIST_ITEM -> navShare(link)
                ObjectTypeDomain.CHANNEL -> navLink(link)
                else -> navLink(link)
            }
        } ?: let { navLink(link) }
    }

    private fun navLink(link: LinkDomain.UrlLinkDomain): NavigationModel =
        NavigationModel(WEB_LINK, mapOf(NavigationModel.Param.LINK to link.address))

    private fun navShare(link: LinkDomain.UrlLinkDomain): NavigationModel =
        NavigationModel(SHARE, mapOf(NavigationModel.Param.LINK to link.address))

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
            if (state.isOnSharePlaylist) {
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

    suspend fun commitPlaylistItems(onCommit: ShareContract.Committer.OnCommit? = null) =
        try {
            val selectedPlaylists = if (state.selectedPlaylists.size > 0) {
                state.selectedPlaylists
            } else {
                playlistOrchestrator.loadList(DefaultFilter(), LOCAL.flatOptions())
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
                                }
                            } else it
                        }
                        ?.let { savedMedia ->
                            state.media = savedMedia
                            selectedPlaylists
                                .filter { it.id != state.editingPlaylistItem?.playlistId } // todo need original playlists (not editingPlaylistItem)
                                .mapNotNull { playlist ->
                                    playlistItemOrchestrator.save(
                                        itemCreator.buildPlayListItem(savedMedia, playlist),
                                        Options(saveSource)
                                    ).takeIf { saveSource == LOCAL && isNew }
                                        ?.also {
                                            prefsWrapper.putLong(
                                                LAST_PLAYLIST_ADDED_TO,
                                                it.playlistId!!
                                            )
                                        }
                                }
                        }
                        ?: listOf()
                    )
                .also { onCommit?.onCommit(ObjectTypeDomain.PLAYLIST_ITEM, it) }
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
                    R.string.menu_support,
                    mapOf(NavigationModel.Param.MEDIA.toString() to media)
                )
            }
    }
}
