package uk.co.sentinelweb.cuer.app.ui.playlists

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.*
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
import uk.co.sentinelweb.cuer.domain.ext.buildTree
import uk.co.sentinelweb.cuer.domain.ext.sort
import java.util.*

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
                                dismiss = {view.repaint()},
                                suggestionsMedia = null,
                                showPin = false,
                                showRoot = true
                            )
                        )
                    }
            }
    }

    private fun setParent(parent: PlaylistDomain, child: PlaylistDomain) {
        // todo check for circular refs!! while parent.parent.id != -1 .
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
                                view.showUndo(
                                    "Deleted playlist: ${playlist.title}",
                                    this@PlaylistsPresenter::undoDelete
                                )
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
                                executeRefresh()
                            }
                        }
                        executeRefresh()
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
        findPlaylist(item)?.id?.apply {
            if (item is ItemContract.Model.ItemModel) {
                view.navigate(PlaylistContract.makeNav(this, null, false, item.source))
            }
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
                playlistOrchestrator.save(itemDomain, LOCAL.flatOptions())
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
                playlistOrchestrator.loadList(OrchestratorContract.AllFilter(), LOCAL.flatOptions())
                    .also {
                        state.treeRoot = it.buildTree()
                            .sort(compareBy{it.node?.title?.lowercase()})
                    }
                    .toMutableList()

            state.playlistStats = playlistStatsOrchestrator
                .loadList(
                    IdListFilter(state.playlists.mapNotNull { if (it.type != APP) it.id else null }),
                    LOCAL.flatOptions()
                )
                .toMutableList()

            val appLists = mutableMapOf(
                newMedia.makeNewItemsHeader() to newMedia.makeNewItemsStats(),
                recentItems.makeRecentItemsHeader() to recentItems.makeRecentItemsStats()
            )
                .apply {
                    if (prefsWrapper.has(LAST_LOCAL_SEARCH)) {
                        put(localSearch.makeSearchHeader(), localSearch.makeSearchItemsStats())
                    }
                }
                .apply {
                    if (prefsWrapper.has(LAST_REMOTE_SEARCH)) {
                        put(remoteSearch.makeSearchHeader(), remoteSearch.makeSearchItemsStats())
                    }
                }

            modelMapper.map2(
                state.playlists
                    .associateWith { pl -> state.playlistStats.find { it.playlistId == pl.id } },
                queue.playlistId,
                appLists,
                prefsWrapper.getLong(PINNED_PLAYLIST),
                state.treeRoot
            ).takeIf { coroutines.mainScopeActive }
                ?.also { view.setList(it, false) }
        } catch (e: Exception) {
            log.e("Load failed", e)
            view.showMessage("Load failed: ${e::class.java.simpleName}")
            view.hideRefresh()
        }
    }

    private fun findPlaylist(item: ItemContract.Model) = state.playlists.find { it.id == item.id }

}