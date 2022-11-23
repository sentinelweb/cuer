package uk.co.sentinelweb.cuer.app.ui.playlists

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.IdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistStatsOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.LocalSearch
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.YoutubeSearch
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.*
import uk.co.sentinelweb.cuer.app.orchestrator.util.PlaylistMergeOrchestrator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.main.MainCommonContract.LastTab.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchMapper
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.ext.buildLookup
import uk.co.sentinelweb.cuer.domain.ext.buildTree
import uk.co.sentinelweb.cuer.domain.ext.isAncestor
import uk.co.sentinelweb.cuer.domain.ext.sort

class PlaylistsPresenter(
    private val view: PlaylistsContract.View,
    private val state: PlaylistsContract.State,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistStatsOrchestrator: PlaylistStatsOrchestrator,
    private val queue: QueueMediatorContract.Producer,
    private val modelMapper: PlaylistsModelMapper,
    private val log: LogWrapper,
    private val toastWrapper: ToastWrapper,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val coroutines: CoroutineContextProvider,
    private val newMedia: NewMediaPlayistInteractor,
    private val recentItems: RecentItemsPlayistInteractor,
    private val localSearch: LocalSearchPlayistInteractor,
    private val remoteSearch: YoutubeSearchPlayistInteractor,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val searchMapper: SearchMapper,
    private val merge: PlaylistMergeOrchestrator,
    private val shareWrapper: AndroidShareWrapper,
    private val recentLocalPlaylists: RecentLocalPlaylists,
    private val starredItems: StarredItemsPlayistInteractor,
    private val unfinishedItems: UnfinishedItemsPlayistInteractor,
    private val res: ResourceWrapper,
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
        state.viewModelScope.launch { executeRefresh() }

        // todo a better job - might refresh too much
        // todo listen for stat changes
        playlistOrchestrator.updates
            .onEach { (op, source, plist) ->
//                log.d("playlist changed: $op, $source, id=${plist.id} items=${plist.items.size}")
                refreshPlaylists()
            }
            .launchIn(coroutines.mainScope)
    }

    override fun onPause() {
        coroutines.cancel()
    }

    //done
    override fun performMove(item: PlaylistsItemMviContract.Model) {
        findPlaylist(item)
            ?.takeIf { it.type != APP }
            ?.apply {
                findPlaylist(item)
                    ?.also { movePlaylist ->
                        view.showPlaylistSelector(
                            PlaylistsMviDialogContract.Config(
                                title = res.getString(R.string.playlist_dialog_title),
                                selectedPlaylists = setOf(),
                                multi = true,
                                itemClick = { playlistSelected, _ ->
                                    playlistSelected
                                        ?.apply { setParent(this, movePlaylist) }
                                        ?: onCreatePlaylist()
                                },
                                confirm = { },
                                dismiss = { view.repaint() },
                                suggestionsMedia = null,
                                showPin = false,
                                showRoot = true,
                                showAdd = false
                            )
                        )
                    }
            }
    }

    //done
    private fun setParent(parent: PlaylistDomain, child: PlaylistDomain) {
        val childNode = state.treeLookup[child.id]!!
        val parentNode = state.treeLookup[parent.id]
        if (parent.id == null || !childNode.isAncestor(parentNode!!)) {
            state.viewModelScope.launch {
                playlistOrchestrator.save(child.copy(parentId = parent.id), LOCAL.flatOptions())
            }
        } else {
            view.repaint()
            toastWrapper.show("That's a circular reference ...")
        }
    }

    // done
    override fun performDelete(item: PlaylistsItemMviContract.Model) {
        state.viewModelScope.launch {
            delay(400)
            findPlaylist(item)
                ?.also { playlist ->
                    if (playlist.type != APP) {
                        val node = state.treeLookup[playlist.id]!!
                        if (node.chidren.size == 0) {
                            state.deletedPlaylist = playlist.id
                                ?.let { playlistOrchestrator.loadById(it, LOCAL.deepOptions()) }
                                ?.apply {
                                    playlistOrchestrator.delete(playlist, LOCAL.flatOptions())
                                    view.notifyItemRemoved(item)
                                    view.showUndo(
                                        "Deleted playlist: ${playlist.title}",
                                        this@PlaylistsPresenter::undoDelete
                                    )
                                }
                                ?: let {
                                    view.showError("Cannot load playlist backup")
                                    null
                                }
                        } else {
                            view.showError("Please delete the children first")
                        }
                    } else if (playlist.id == LocalSearch.id || playlist.id == YoutubeSearch.id) {
                        val isLocal = playlist.id == LocalSearch.id
                        val type = searchMapper.searchTypeText(isLocal)
                        val lastLocalSearch = prefsWrapper.lastLocalSearch
                        val lastRemoteSearch = prefsWrapper.lastRemoteSearch
                        if (isLocal) prefsWrapper.lastLocalSearch = null
                        else prefsWrapper.lastRemoteSearch = null
                        if (!isLocal) remoteSearch.clearCached()
                        view.showUndo("Deleted $type search") {
                            if (isLocal) prefsWrapper.lastLocalSearch = lastLocalSearch
                            else prefsWrapper.lastRemoteSearch = lastRemoteSearch
                            state.viewModelScope.launch {
                                executeRefresh()
                            }
                        }
                        executeRefresh()
                    }
                } ?: let { view.showError("Cannot delete playlist") }
        }
    }

    // done
    override fun performOpen(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {
        if (item is PlaylistsItemMviContract.Model.Item) {
            recentLocalPlaylists.addRecentId(item.id)
            prefsWrapper.lastBottomTab = PLAYLIST.ordinal
            view.navigate(
                PlaylistContract.makeNav(
                    item.id, null, false, item.source,
                    imageUrl = item.thumbNailUrl
                ), sourceView
            )
        }
    }

    // done
    override fun onItemImageClicked(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {
        findPlaylist(item)?.id?.apply {
            if (item is PlaylistsItemMviContract.Model.Item) {
                view.navigate(
                    PlaylistContract.makeNav(
                        this, null, false, item.source,
                        imageUrl = item.thumbNailUrl
                    ), sourceView
                )
            }
        }
    }

    // done
    override fun performPlay(
        item: PlaylistsItemMviContract.Model,
        external: Boolean,
        sourceView: ItemContract.ItemView
    ) {
        if (!external) {
            if (item is PlaylistsItemMviContract.Model.Item) {
                view.navigate(
                    PlaylistContract.makeNav(
                        item.id, null, true, item.source,
                        imageUrl = item.thumbNailUrl
                    ), sourceView
                )
            }
        } else {
            findPlaylist(item)
                ?.takeIf { it.type == PLATFORM }
                ?.apply { ytJavaApi.launchPlaylist(platformId!!) }
                ?: let { view.showError("Cannot launch playlist") }
        }
    }

    // done
    override fun performStar(item: PlaylistsItemMviContract.Model) {
        state.viewModelScope.launch {
            findPlaylist(item)
                ?.takeIf { (it.id != null) && (it.id ?: 0) > 0 }
                ?.let { it.copy(starred = !it.starred) }
                ?.also { playlistOrchestrator.save(it, LOCAL.flatOptions()) }
        }
    }

    // done
    override fun performShare(item: PlaylistsItemMviContract.Model) {
        findPlaylist(item)
            ?.takeIf { (it.id != null) && (it.id ?: 0) > 0 && it.type != APP }
            ?.let { itemDomain ->
                coroutines.mainScope.launch {
                    playlistOrchestrator.loadById(itemDomain.id!!, LOCAL.deepOptions())
                        ?.also { shareWrapper.share(it) }
                        ?: view.showError("Couldn't load playlist ...")
                }
            }
    }

    // done
    override fun performEdit(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {
        view.navigate(
            PlaylistEditContract.makeNav(
                item.id,
                LOCAL,
                (item as PlaylistsItemMviContract.Model.Item).thumbNailUrl
            ), sourceView
        )
    }

    // done
    override fun onCreatePlaylist() {
        view.navigate(PlaylistEditContract.makeCreateNav(LOCAL), null)
    }

    override fun performMerge(item: PlaylistsItemMviContract.Model) {
        findPlaylist(item)
            ?.takeIf { it.type != APP }
            ?.apply {
                findPlaylist(item)?.also { delPlaylist ->
                    view.showPlaylistSelector(
                        PlaylistsMviDialogContract.Config(
                            title = res.getString(R.string.playlist_dialog_title),
                            selectedPlaylists = setOf(),
                            multi = true,
                            itemClick = { p, _ -> merge(p!!, delPlaylist) },
                            confirm = { },
                            dismiss = { },
                            suggestionsMedia = null,
                            showPin = false,
                            showAdd = false
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
                view.showError("Cannot merge this playlist")
            }
        }
    }

    // done
    override fun moveItem(fromPosition: Int, toPosition: Int) {
        if (state.dragFrom == null) {
            state.dragFrom = fromPosition
        }
        state.dragTo = toPosition
    }

    // done
    override fun commitMove() {
        if (state.dragFrom != null && state.dragTo != null) {
            //todo save move ..
        } else {
            refreshPlaylists()
        }
        state.dragFrom = null
        state.dragTo = null
    }

    // done
    override fun undoDelete() {
        state.deletedPlaylist?.let { itemDomain ->
            state.viewModelScope.launch {
                playlistOrchestrator.save(itemDomain, LOCAL.deepOptions())
                state.deletedPlaylist = null
                executeRefresh()
            }
        }
    }

    // done
    private fun refreshPlaylists() {
        state.viewModelScope.launch { executeRefresh() }
    }

    // done
    private suspend fun executeRefresh() {
        try {
            state.playlists =
                playlistOrchestrator.loadList(AllFilter, LOCAL.flatOptions())
                    .also {
                        state.treeRoot = it.buildTree()
                            .sort(compareBy { it.node?.title?.lowercase() })
                        state.treeLookup = state.treeRoot.buildLookup()
                    }
                    .toMutableList()

            state.playlistStats = playlistStatsOrchestrator
                .loadList(
                    IdListFilter(state.playlists.mapNotNull { if (it.type != APP) it.id else null }),
                    LOCAL.flatOptions()
                )
                .toMutableList()

            val appLists = mutableListOf(newMedia, recentItems, starredItems, unfinishedItems)
                .apply { if (prefsWrapper.hasLocalSearch) add(localSearch) }
                .apply { if (prefsWrapper.hasRemoteSearch) add(remoteSearch) }
                .map { it.makeHeader() to it.makeStats() }
                .toMap()

            modelMapper.map(
                state.playlists
                    .associateWith { pl -> state.playlistStats.find { it.playlistId == pl.id } },
                queue.playlistId,
                appLists,
                recentLocalPlaylists.buildRecentSelectionList(),
                prefsWrapper.pinnedPlaylistId,
                state.treeRoot
            ).takeIf { coroutines.mainScopeActive }
                ?.also { view.setList(it, false) }
        } catch (e: Exception) {
            log.e("Load failed", e)
            view.showError("Load failed: ${e::class.java.simpleName}")
            view.hideRefresh()
        }
    }

    private fun findPlaylist(item: PlaylistsItemMviContract.Model) = state.playlists.find { it.id == item.id }

}