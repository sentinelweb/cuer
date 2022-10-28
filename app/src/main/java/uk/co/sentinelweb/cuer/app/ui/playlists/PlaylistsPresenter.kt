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
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.search.SearchMapper
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.*
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
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
    private val prefsWrapper: GeneralPreferencesWrapper,
    private val coroutines: CoroutineContextProvider,
    private val newMedia: NewMediaPlayistInteractor,
    private val recentItems: RecentItemsPlayistInteractor,
    private val localSearch: LocalSearchPlayistInteractor,
    private val remoteSearch: YoutubeSearchPlayistInteractor,
    private val ytJavaApi: YoutubeJavaApiWrapper,
    private val searchMapper: SearchMapper,
    private val merge: PlaylistMergeOrchestrator,
    private val shareWrapper: ShareWrapper,
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
                log.d("playlist changed: $op, $source, id=${plist.id} items=${plist.items.size}")
                refreshPlaylists()
            }
            .launchIn(coroutines.mainScope)
    }

    override fun onPause() {
        coroutines.cancel()
    }

    override fun performMove(item: ItemContract.Model) {
        findPlaylist(item)
            ?.takeIf { it.type != APP }
            ?.apply {
                findPlaylist(item)
                    ?.also { movePlaylist ->
                        view.showPlaylistSelector(
                            PlaylistsDialogContract.Config(
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

    // todo review this
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

    override fun performDelete(item: ItemContract.Model) {
        state.viewModelScope.launch {
            delay(400)
            findPlaylist(item)
                ?.also { playlist ->
                    if (playlist.type != APP) {
                        val node = state.treeLookup[playlist.id]!!
                        if (node.chidren.size == 0) {
                            state.deletedPlaylist = playlist.id
                                ?.let { playlistOrchestrator.load(it, LOCAL.deepOptions()) }
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
                        val key = if (isLocal) LAST_LOCAL_SEARCH else LAST_REMOTE_SEARCH
                        val lastSearch = prefsWrapper.getString(key, null)
                        prefsWrapper.remove(key)
                        if (!isLocal) remoteSearch.clearCached()
                        view.showUndo("Deleted $type search") {
                            prefsWrapper.putString(key, lastSearch!!)
                            state.viewModelScope.launch {
                                executeRefresh()
                            }
                        }
                        executeRefresh()
                    }
                } ?: let { view.showError("Cannot delete playlist") }
        }
    }

    override fun performOpen(item: ItemContract.Model, sourceView: ItemContract.ItemView) {
        if (item is ItemContract.Model.ItemModel) {
            view.navigate(
                PlaylistContract.makeNav(
                    item.id, null, false, item.source,
                    imageUrl = item.thumbNailUrl
                ), sourceView
            )
        }
    }

    override fun onItemImageClicked(item: ItemContract.Model, sourceView: ItemContract.ItemView) {
        findPlaylist(item)?.id?.apply {
            if (item is ItemContract.Model.ItemModel) {
                view.navigate(
                    PlaylistContract.makeNav(
                        this, null, false, item.source,
                        imageUrl = item.thumbNailUrl
                    ), sourceView
                )
            }
        }
    }

    override fun performPlay(
        item: ItemContract.Model,
        external: Boolean,
        sourceView: ItemContract.ItemView
    ) {
        if (!external) {
            if (item is ItemContract.Model.ItemModel) {
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

    override fun performStar(item: ItemContract.Model) {
        state.viewModelScope.launch {
            findPlaylist(item)
                ?.takeIf { (it.id != null) && (it.id ?: 0) > 0 }
                ?.let { it.copy(starred = !it.starred) }
                ?.also { playlistOrchestrator.save(it, LOCAL.flatOptions()) }
        }
    }

    override fun performShare(item: ItemContract.Model) {
        findPlaylist(item)
            ?.takeIf { (it.id != null) && (it.id ?: 0) > 0 && it.type != APP }
            ?.let { itemDomain ->
                coroutines.mainScope.launch {
                    playlistOrchestrator.load(itemDomain.id!!, LOCAL.deepOptions())
                        ?.also { shareWrapper.share(it) }
                        ?: view.showError("Couldn't load playlist ...")
                }
            }
    }

    override fun performEdit(item: ItemContract.Model, sourceView: ItemContract.ItemView) {
        view.navigate(
            PlaylistEditContract.makeNav(
                item.id,
                LOCAL,
                (item as ItemContract.Model.ItemModel).thumbNailUrl
            ), sourceView
        )
    }

    override fun onCreatePlaylist() {
        view.navigate(PlaylistEditContract.makeCreateNav(LOCAL), null)
    }

    override fun performMerge(item: ItemContract.Model) {
        findPlaylist(item)
            ?.takeIf { it.type != APP }
            ?.apply {
                findPlaylist(item)?.also { delPlaylist ->
                    view.showPlaylistSelector(
                        PlaylistsDialogContract.Config(
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

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        if (state.dragFrom == null) {
            state.dragFrom = fromPosition
        }
        state.dragTo = toPosition
    }

    override fun commitMove() {
        if (state.dragFrom != null && state.dragTo != null) {
            //toastWrapper.show("todo save move ..")
        } else {
            //toastWrapper.show("Move failed ..")
            refreshPlaylists()
        }
        state.dragFrom = null
        state.dragTo = null
    }

    override fun undoDelete() {
        state.deletedPlaylist?.let { itemDomain ->
            state.viewModelScope.launch {
                playlistOrchestrator.save(itemDomain, LOCAL.deepOptions())
                state.deletedPlaylist = null
                executeRefresh()
            }
        }
    }

    private fun refreshPlaylists() {
        state.viewModelScope.launch { executeRefresh() }
    }

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
                .apply { if (prefsWrapper.has(LAST_LOCAL_SEARCH)) add(localSearch) }
                .apply { if (prefsWrapper.has(LAST_REMOTE_SEARCH)) add(remoteSearch) }
                .map { it.makeHeader() to it.makeStats() }
                .toMap()

            modelMapper.map(
                state.playlists
                    .associateWith { pl -> state.playlistStats.find { it.playlistId == pl.id } },
                queue.playlistId,
                appLists,
                recentLocalPlaylists.buildRecentSelectionList(),
                prefsWrapper.getLong(PINNED_PLAYLIST),
                state.treeRoot
            ).takeIf { coroutines.mainScopeActive }
                ?.also { view.setList(it, false) }
        } catch (e: Exception) {
            log.e("Load failed", e)
            view.showError("Load failed: ${e::class.java.simpleName}")
            view.hideRefresh()
        }
    }

    private fun findPlaylist(item: ItemContract.Model) = state.playlists.find { it.id == item.id }

}