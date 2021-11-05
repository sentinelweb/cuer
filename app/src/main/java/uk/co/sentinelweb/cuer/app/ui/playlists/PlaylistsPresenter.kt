package uk.co.sentinelweb.cuer.app.ui.playlists

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.IdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.LOCAL_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.REMOTE_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.LocalSearchPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.NewMediaPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RecentItemsPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RemoteSearchPlayistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.util.PlaylistMergeOrchestrator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchMapper
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.*
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.ext.buildLookup
import uk.co.sentinelweb.cuer.domain.ext.buildTree
import java.util.*
import java.util.Locale.getDefault

class PlaylistsPresenter(
    private val view: PlaylistsContract.View,
    private val state: PlaylistsContract.State,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistStatsOrchestrator: PlaylistStatsOrchestrator,
    private val queue: QueueMediatorContract.Producer,
    private val modelMapper: PlaylistsModelMapper,
    private val log: LogWrapper,
    private val toastWrapper: ToastWrapper,
    private val prefsWrapper: GeneralPreferencesWrapper,
    private val coroutines: CoroutineContextProvider,
    private val newMedia: NewMediaPlayistInteractor,
    private val recentItems: RecentItemsPlayistInteractor,
    private val localSearch: LocalSearchPlayistInteractor,
    private val remoteSearch: RemoteSearchPlayistOrchestrator,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val searchMapper: SearchMapper,
    private val merge: PlaylistMergeOrchestrator,
    private val shareWrapper: ShareWrapper,
) : PlaylistsContract.Presenter {

    init {
        log.tag(this)
    }

    override fun refreshList() {
        refreshPlaylists()
    }

    override fun setFocusMedia(mediaDomain: MediaDomain) {
        //state.addedMedia = mediaDomain
    }

    override fun onResume(parentId: Long?) {
        state.treeCurrentNodeId = parentId
        state.viewModelScope.launch { executeRefresh(true) }

        // todo a better job - might refresh too much
        // todo listen for stat changes
        playlistOrchestrator.updates
            .onEach { (op, source, plist) ->
                log.d("playlist changed: $op, $source, id=${plist.id} items=${plist.items.size}")
                refreshPlaylists()
            }
            .launchIn(coroutines.mainScope)
    }

    override fun onPause() {
        coroutines.cancel()
    }

    override fun onItemSwipeRight(item: ItemContract.Model) {
        findPlaylist(item)
            ?.takeIf { it.type != APP }
            ?.apply {
                findPlaylist(item)
                    ?.also { delPlaylist ->
                        view.showPlaylistSelector(
                            PlaylistsDialogContract.Config(
                                selectedPlaylists = setOf(),
                                multi = true,
                                itemClick = { p, _ -> p?.apply { setParent(this, delPlaylist) } },
                                confirm = { },
                                dismiss = { showCurrentNode() },
                                suggestionsMedia = null,
                                showPin = false,
                            )
                        )
                    }
            }
    }

    private fun setParent(parent: PlaylistDomain, child: PlaylistDomain) {
        state.viewModelScope.launch {
            playlistOrchestrator.save(child.copy(parentId = parent.id), LOCAL.flatOptions())
        }
    }

    override fun onItemSwipeLeft(item: ItemContract.Model) {
        state.viewModelScope.launch {
            delay(400)
            findPlaylist(item)
                ?.let { playlist ->
                    if (playlist.type != APP) {
                        state.deletedPlaylist = playlist.id
                            ?.let { playlistOrchestrator.load(it, LOCAL.deepOptions()) }
                            ?.apply {
                                playlistOrchestrator.delete(playlist, LOCAL.flatOptions())
                                view.showUndo("Deleted playlist: ${playlist.title}", this@PlaylistsPresenter::undoDelete)
                            }
                            ?: let {
                                view.showMessage("Cannot load playlist backup")
                                null
                            }
                    } else if (playlist.id == LOCAL_SEARCH_PLAYLIST || playlist.id == REMOTE_SEARCH_PLAYLIST) {
                        val isLocal = playlist.id == LOCAL_SEARCH_PLAYLIST
                        val type = searchMapper.searchTypeText(isLocal)
                        val key = if (isLocal) LAST_LOCAL_SEARCH else LAST_REMOTE_SEARCH
                        val lastSearch = prefsWrapper.getString(key, null)
                        prefsWrapper.remove(key)
                        if (!isLocal) remoteSearch.clearCached()
                        view.showUndo("Deleted $type search") {
                            prefsWrapper.putString(key, lastSearch!!)
                            state.viewModelScope.launch {
                                executeRefresh(false)
                            }
                        }
                        executeRefresh(false)
                    }
                } ?: let { view.showMessage("Cannot delete playlist") }

        }
    }

    override fun onItemClicked(item: ItemContract.Model) {
        if (item is ItemContract.Model.ItemModel) {
            view.navigate(PlaylistContract.makeNav(item.id, null, false, item.source))
        }
    }

    override fun onItemImageClicked(item: ItemContract.Model) {
        findPlaylist(item)
            ?.takeIf { state.treeLookup[it.id]?.chidren?.size ?: 0 > 0 }
            ?.also {
                state.treeCurrentNodeId = it.id
                showCurrentNode()
            }
            ?: findPlaylist(item)?.id?.apply {
                if (item is ItemContract.Model.ItemModel) {
                    view.navigate(PlaylistContract.makeNav(this, null, false, item.source))
                }
            }
    }

    override fun onUpClicked() {
        if (state.treeCurrentNodeId != null) {
            state.treeCurrentNodeId = state.treeLookup[state.treeCurrentNodeId]?.let { it.parent?.node?.id }
            showCurrentNode()
        }
    }

    override fun onItemPlay(item: ItemContract.Model, external: Boolean) {
        if (!external) {
            if (item is ItemContract.Model.ItemModel) {
                view.navigate(PlaylistContract.makeNav(item.id, null, true, item.source))
            }
        } else {
            findPlaylist(item)
                ?.takeIf { it.type == PLATFORM }
                ?.apply { ytJavaApi.launchPlaylist(platformId!!) }
                ?: let { view.showMessage("Cannot launch playlist") }
        }
    }

    override fun onItemStar(item: ItemContract.Model) {
        state.viewModelScope.launch {
            findPlaylist(item)
                ?.takeIf { it.id != null && it.id ?: 0 > 0 }
                ?.let { it.copy(starred = !it.starred) }
                ?.also { playlistOrchestrator.save(it, LOCAL.flatOptions()) }
        }
    }

    override fun onItemShare(item: ItemContract.Model) {
        findPlaylist(item)
            ?.takeIf { it.id != null && it.id ?: 0 > 0 && it.type != APP }
            ?.let { itemDomain ->
                coroutines.mainScope.launch {
                    playlistOrchestrator.load(itemDomain.id!!, LOCAL.deepOptions())
                        ?.also { shareWrapper.share(it) }
                        ?: view.showMessage("Couldn't load playlist ...")
                }
            }
    }

    override fun onEdit(item: ItemContract.Model) {
        view.navigate(PlaylistEditContract.makeNav(item.id, LOCAL))
    }

    override fun onMerge(item: ItemContract.Model) {
        findPlaylist(item)
            ?.takeIf { it.type != APP }
            ?.apply {
                findPlaylist(item)?.also { delPlaylist ->
                    view.showPlaylistSelector(
                        PlaylistsDialogContract.Config(
                            selectedPlaylists = setOf(),
                            multi = true,
                            itemClick = { p, _ -> merge(p!!, delPlaylist) },
                            confirm = { },
                            dismiss = { },
                            suggestionsMedia = null,
                            showPin = false,
                        )
                    )
                }
            }
    }

    private fun merge(thisPlaylist: PlaylistDomain, delPlaylist: PlaylistDomain) {
        coroutines.mainScope.launch {
            if (merge.checkMerge(thisPlaylist, delPlaylist)) {
                merge.merge(thisPlaylist, delPlaylist)
            } else {
                view.showMessage("Cannot merge this playlist")
            }
        }
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        if (state.dragFrom == null) {
            state.dragFrom = fromPosition
        }
        state.dragTo = toPosition
    }

    override fun commitMove() {
        if (state.dragFrom != null && state.dragTo != null) {
            toastWrapper.show("todo save move ..")
        } else {
            toastWrapper.show("Move failed ..")
            refreshPlaylists()
        }
        state.dragFrom = null
        state.dragTo = null
    }

    override fun undoDelete() {
        state.deletedPlaylist?.let { itemDomain ->
            state.viewModelScope.launch {
                playlistOrchestrator.save(itemDomain, LOCAL.flatOptions())
                state.deletedPlaylist = null
                executeRefresh(false)
            }
        }
    }

    private fun refreshPlaylists() {
        state.viewModelScope.launch { executeRefresh(false) }
    }

    private suspend fun executeRefresh(focusCurrent: Boolean) {
        try {
            state.playlists = playlistOrchestrator.loadList(OrchestratorContract.AllFilter(), LOCAL.flatOptions())
                .also {
                    state.treeRoot = it.buildTree()
                    state.treeLookup = state.treeRoot.buildLookup()
                }
                .toMutableList()
                .apply { add(0, newMedia.makeNewItemsHeader()) }
                .apply { add(1, recentItems.makeRecentItemsHeader()) }
                .apply {
                    if (prefsWrapper.has(LAST_LOCAL_SEARCH)) {
                        add(2, localSearch.makeSearchHeader())
                    }
                }
                .apply {
                    if (prefsWrapper.has(LAST_REMOTE_SEARCH)) {
                        add(2, remoteSearch.makeSearchHeader())
                    }
                }

            state.playlistStats = playlistStatsOrchestrator
                .loadList(IdListFilter(state.playlists.mapNotNull { if (it.type != APP) it.id else null }), LOCAL.flatOptions())
                .toMutableList()
                .apply { add(newMedia.makeNewItemsStats()) }
                .apply { add(recentItems.makeRecentItemsStats()) }
                .apply {
                    if (prefsWrapper.has(LAST_LOCAL_SEARCH)) {
                        add(2, localSearch.makeSearchItemsStats())
                    }
                }
                .apply {
                    if (prefsWrapper.has(LAST_REMOTE_SEARCH)) {
                        add(3, remoteSearch.makeSearchItemsStats())
                    }
                }

            state.playlistsDisplay = buildDisplayList()

            showCurrentNode()
                .takeIf { focusCurrent }
                ?.let {
                    state.playlists.apply {
                        prefsWrapper.getPairNonNull(LAST_PLAYLIST_VIEWED, NO_PLAYLIST.toPair())
                            ?.toIdentifier()
                            ?.let { focusId -> view.scrollToItem(indexOf(find { it.id == focusId.id })) }
                    }
                }
        } catch (e: Exception) {
            log.e("Load failed", e)
            view.showMessage("Load failed: ${e::class.java.simpleName}")
            view.hideRefresh()
        }
    }

    private fun mapModel(
        it: Map<PlaylistDomain, PlaylistStatDomain?>,
    ) = modelMapper.map(
        it,
        queue.playlistId,
        prefsWrapper.getLong(PINNED_PLAYLIST),
        state.treeCurrentNodeId,
        state.treeLookup
    )

    private fun buildDisplayList(): List<PlaylistDomain> =
        if (state.treeCurrentNodeId == null) {
            val pinnedId = prefsWrapper.getLong(PINNED_PLAYLIST)
            state.playlists
                .filter { it.type == APP || it.starred || it.id == pinnedId }
                .sortedWith(compareBy({ it.id != pinnedId }, { it.type != APP }, { !it.starred }, { it.title.toLowerCase(getDefault()) }))
                .toMutableList()
                .let { list -> list to (list.map { it.id }.toSet()) }
                .let { (list, idset) ->
                    list.addAll(
                        state.treeRoot.chidren
                            .filter { !idset.contains(it.node!!.id) }
                            .map { it.node!! }
                            .sortedBy { it.title.toLowerCase(getDefault()) }
                    )
                    list
                }
        } else {
            state.treeLookup[state.treeCurrentNodeId]?.chidren
                ?.map { it.node!! }
                ?.sortedBy { it.title.toLowerCase(getDefault()) }
                ?: listOf()
        }

    private fun showCurrentNode() {
        state.playlistsDisplay = buildDisplayList()
        state.playlistsDisplay
            .associateWith { pl -> state.playlistStats.find { it.playlistId == pl.id } }
            .let { mapModel(it) }
            .takeIf { coroutines.mainScopeActive }
            ?.also { view.setList(it, false) }
    }

    private fun findPlaylist(item: ItemContract.Model) = state.playlists.find { it.id == item.id }


}