package uk.co.sentinelweb.cuer.app.ui.playlists

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.NewMediaPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.RecentItemsPlayistInteractor
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.orchestrator.toPair
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.CURRENT_PLAYLIST
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM

class PlaylistsPresenter(
    private val view: PlaylistsContract.View,
    private val state: PlaylistsContract.State,
    private val playlistRepository: PlaylistDatabaseRepository,
    private val queue: QueueMediatorContract.Producer,
    private val modelMapper: PlaylistsModelMapper,
    private val log: LogWrapper,
    private val toastWrapper: ToastWrapper,
    private val prefsWrapper: SharedPrefsWrapper<GeneralPreferences>,
    private val coroutines: CoroutineContextProvider,
    private val newMedia: NewMediaPlayistInteractor,
    private val recentItems: RecentItemsPlayistInteractor,
    private val ytJavaApi: YoutubeJavaApiWrapper
) : PlaylistsContract.Presenter {

    override fun initialise() {

    }

    override fun refreshList() {
        refreshPlaylists()
    }

    override fun setFocusMedia(mediaDomain: MediaDomain) {
        //state.addedMedia = mediaDomain
    }

    override fun destroy() {
    }

    override fun onItemSwipeRight(item: ItemContract.Model) {
        view.gotoEdit(item.id, LOCAL)
    }

    override fun onItemSwipeLeft(item: ItemContract.Model) {
        state.viewModelScope.launch {
            delay(400)
            state.playlists.apply {
                find { it.id == item.id }
                    ?.takeIf { it.type != APP }
                    ?.let { playlist ->
                        state.deletedPlaylist = playlist.id
                            ?.let { playlistRepository.load(it, flat = false) }
                            ?.takeIf { it.isSuccessful }
                            ?.data
                            ?.apply {
                                playlistRepository.delete(playlist, emit = true)
                                view.showDeleteUndo("Deleted playlist: ${playlist.title}")
                                executeRefresh(false, false)
                            }
                            ?: let {
                                view.showMessage("Cannot load playlist backup")
                                null
                            }
                    } ?: let { view.showMessage("Cannot delete playlist") }
            }
        }
    }

    override fun onItemClicked(item: ItemContract.Model) {
        view.gotoPlaylist(item.id, false, item.source)// todo map to list (add to model)
    }

    override fun onItemPlay(item: ItemContract.Model, external: Boolean) {
        if (!external) {
            view.gotoPlaylist(item.id, true, item.source)
        } else {
            state.playlists
                .find { it.id == item.id }
                ?.takeIf { it.type == PLATFORM }
                ?.apply { ytJavaApi.launchPlaylist(platformId!!) }
                ?: let { view.showMessage("Cannot launch playlist") }
        }
    }

    override fun onItemStar(item: ItemContract.Model) {
        toastWrapper.show("todo: star ${item.id}")
    }

    override fun onItemShare(item: ItemContract.Model) {
        toastWrapper.show("share: ${item.title}")
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
                playlistRepository.save(itemDomain)
                state.deletedPlaylist = null
                executeRefresh(false)
            }
        }
    }

    private fun refreshPlaylists() {
        state.viewModelScope.launch { executeRefresh(false) }
    }

    private suspend fun executeRefresh(focusCurrent: Boolean, animate: Boolean = true) {
        state.playlists = playlistRepository.loadList(null)
            .takeIf { it.isSuccessful }
            ?.data
            ?.sortedWith(compareBy({ !it.starred }, { it.title.toLowerCase() }))
            ?.toMutableList()
            ?.apply { add(0, newMedia.makeNewItemsHeader()) }
            ?.apply { add(1, recentItems.makeRecentItemsHeader()) }
            ?: listOf()

        state.playlistStats = playlistRepository
            .loadPlaylistStatList(state.playlists.mapNotNull { if (it.type != APP) it.id else null })
            .data
            ?.toMutableList()
            ?.apply { add(newMedia.makeNewItemsStats()) }
            ?.apply { add(recentItems.makeRecentItemsStats()) }
            ?: listOf()

        state.playlists
            .associateWith { pl -> state.playlistStats.find { it.playlistId == pl.id } }
            .let { modelMapper.map(it, queue.playlistId, true) }
            .takeIf { coroutines.mainScopeActive }
            ?.also { view.setList(it, animate) }
            ?.takeIf { focusCurrent }
            ?.let {
                state.playlists.apply {
                    prefsWrapper.getPairNonNull(CURRENT_PLAYLIST, NO_PLAYLIST.toPair())
                        ?.toIdentifier()
                        ?.let { focusId -> view.scrollToItem(indexOf(find { it.id == focusId.id })) }
                }
            }
    }

    override fun onResume() {
        state.viewModelScope.launch { executeRefresh(true) }
        // todo a better job - might refresh too much
        // todo listen for stat changes
        playlistRepository.updates
            .onEach { refreshPlaylists() }
            .let { coroutines.mainScope }

    }

    override fun onPause() {
        coroutines.cancel()
    }
}