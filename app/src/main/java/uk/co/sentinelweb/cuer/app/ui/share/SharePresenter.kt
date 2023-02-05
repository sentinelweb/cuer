package uk.co.sentinelweb.cuer.app.ui.share

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.exception.NoDefaultPlaylistException
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.MediaIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Options
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.NAV_BACK
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.NAV_FINISH
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.*
import uk.co.sentinelweb.cuer.domain.ext.domainJsonSerializer

class SharePresenter constructor(
    private val view: ShareContract.View,
    private val coroutines: CoroutineContextProvider,
    private val toast: ToastWrapper,
    private val queue: QueueMediatorContract.Producer,
    private val state: ShareContract.State,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val log: LogWrapper,
    private val mapper: ShareModelMapper,
    private val prefsWrapper: MultiPlatformPreferencesWrapper,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val timeProvider: TimeProvider,
    private val shareStrings: ShareContract.ShareStrings,
    private val recentLocalPlaylists: RecentLocalPlaylists,
//    private val platformIdFilter: PlatformIdFilter
) : ShareContract.Presenter {

    init {
        log.tag(this)
    }

    override fun onStop() {
        coroutines.cancel()
    }
// abandoning commit listening need to wait for all playlist items before cqllingafter commit
// also need to hold the state params from the finish method as they are also passed to after commit
// basically a bigger refactor is needed here to get it to work
// share presenter has gotten too complex and need to be re-designed
//    private fun listenForCommit() {
//        playlistOrchestrator.updates
//            .filter { platformIdFilter.compareTo(it) }
//            .onEach { /*aftercommit*/ }
//            .launchIn(coroutines.mainScope)
//
//        playlistItemOrchestrator.updates
//            .filter { platformIdFilter.compareTo(it) }
//            .onEach { /*aftercommit*/ }
//            .launchIn(coroutines.mainScope)
//    }

    override fun setPlaylistParent(cat: CategoryDomain?, parentId: Identifier<GUID>) {
        state.category = cat
        state.parentPlaylistId = parentId
    }

    override fun onReady(ready: Boolean) {
        state.ready = ready
        // listenForCommit()
        mapModel()
    }

    private fun mapModel() {
        (state.scanResult
            ?.also {
                if ((it.type == MEDIA && (!it.isNew || it.isOnPlaylist)) || (it.type == PLAYLIST && !it.isNew))
                    view.warning(
                        shareStrings.errorExists(
                            it.type.toString().lowercase()
                                .replaceFirstChar { char -> char.uppercase() })
                    )
            }
            ?.let {
                val canCommit = view.canCommit(state.scanResult?.type)
                log.d("canCommit; $canCommit: state.scanResult?.type:${state.scanResult?.type}")
                mapper.mapShareModel(state, ::finish, canCommit)
            }
            ?: mapper.mapEmptyModel(::finish))
            .apply {
                state.model = this
                view.setData(this)
            }
    }

    override fun linkError(clipText: String?) {
        log.e("cannot add url : $clipText")
        clipText?.apply {
            view.warning("Cannot add : ${this.take(100)}")
            view.setData(mapper.mapEmptyModel(::finish))
        } ?: run {
            view.warning("Nothing to add ...")
            view.setData(mapper.mapEmptyModel(::finish))
        }
    }

    override fun scanResult(result: ScanContract.Result) {
        state.scanResult = result
        when (result.type) {
            MEDIA -> (result.result as MediaDomain).let {
                val itemDomain =
                    PlaylistItemDomain(null, it, timeProvider.instant(), 0, false, null)
                view.showMedia(
                    itemDomain,
                    state.parentPlaylistId
                )
//                platformIdFilter.targetDomainClass = PlaylistItemDomain::class
//                platformIdFilter.targetPlatformId = it.platformId
                mapModel()
            }

            PLAYLIST -> (result.result as PlaylistDomain).let { playlist ->
                playlist.id?.let { id ->
                    coroutines.mainScope.launch {
                        if (result.isNew && state.category?.image?.url?.isNotEmpty() ?: false) {
                            playlist.copy(
                                image = state.category?.image ?: playlist.image,
                                thumb = state.category?.image ?: playlist.thumb,
                                config = playlist.config.copy(
                                    description = state.category?.description
                                )
                            ).apply {
                                playlistOrchestrator.save(
                                    this,
                                    Options(MEMORY, flat = true, emit = false)
                                )
                            }
                        }
//                        platformIdFilter.targetDomainClass = PlaylistDomain::class
//                        platformIdFilter.targetPlatformId = playlist.platformId
                        view.showPlaylist(
                            //id.toIdentifier(if (result.isNew) MEMORY else LOCAL),
                            id.copy(source = if (result.isNew) MEMORY else LOCAL),
                            state.parentPlaylistId
                        )
                        mapModel()
                    }
                } ?: throw IllegalStateException("Playlist needs an id (isNew = MEMORY)")
            }

            else -> throw IllegalArgumentException("unsupported type: ${result.type}")
        }
    }

    override fun isAlreadyScanned(urlOrText: String): Boolean {
        return state.scanResult
            ?.let {
                it.url.substring(it.url.indexOf("://") // sometimes protocol is prepended
                    .takeIf { it > -1 }
                    ?.let { it + 3 }
                    ?: 0
                )
            }
            ?.let { url -> urlOrText.contains(url) }
            ?: false
    }

    override fun serializeState(): String =
        domainJsonSerializer.encodeToString(ShareContract.State.serializer(), state)

    override fun restoreState(s: String) {
        domainJsonSerializer.decodeFromString(ShareContract.State.serializer(), s)
            .apply {
                state.parentPlaylistId = parentPlaylistId
                state.ready = ready
                state.scanResult = scanResult
                state.model = state.scanResult
                    ?.let { mapper.mapShareModel(state, ::finish, view.canCommit(state.scanResult?.type)) }
                    ?: mapper.mapEmptyModel(::finish)
            }
    }

    override fun afterItemEditNavigation() {
        if (state.scanResult?.type == PLAYLIST) {
            view.navigate(NavigationModel(NAV_BACK))
        } else {
            view.navigate(NavigationModel(NAV_FINISH))
        }
    }

    private fun finish(add: Boolean, play: Boolean, forward: Boolean) {
        coroutines.mainScope.launch {
            if (add) {// fixme if playlist items are changed then they aren't saved here
                if (view.canCommit(state.scanResult?.type)) {
                    view.commit(object : ShareCommitter.AfterCommit {
                        override suspend fun onCommit(type: ObjectTypeDomain, data: List<*>) {
                            afterCommit(type, data, play, forward)
                        }
                    })

                } else {
                    view.warning("Can't save from here")
                }
            } else {
                when (state.scanResult?.type) {
                    MEDIA ->
                        (state.scanResult?.result as MediaDomain)
                            .let { playlistItemOrchestrator.loadList(MediaIdListFilter(listOf(it.id!!.id)), LOCAL.flatOptions()) }
                            .let { afterCommit(PLAYLIST_ITEM, it, play, forward) }

                    PLAYLIST -> afterCommit(PLAYLIST, listOf(state.scanResult?.result as PlaylistDomain), play, forward)

                    else -> throw IllegalArgumentException("unsupported type: ${state.scanResult?.type}")
                }
            }
        }
    }

    override fun onDestinationChange() {
        view.hideWarning()
    }

    private suspend fun afterCommit(
        type: ObjectTypeDomain,
        data: List<*>,
        play: Boolean,
        forward: Boolean
    ) {
        try {
            val isConnected = ytContextHolder.isConnected()
            val currentPlaylistId = prefsWrapper.currentPlayingPlaylistId
            val playId: Pair<Identifier<GUID>, Identifier<GUID>?> = when (type) {
                PLAYLIST -> selectPlaylist(data as List<PlaylistDomain>, currentPlaylistId)

                PLAYLIST_ITEM -> selectPlaylistItem(data as List<PlaylistItemDomain>, currentPlaylistId)

                else -> throw java.lang.IllegalStateException("Unsupported type")
            }
            recentLocalPlaylists.addRecentId(playId.first.id)
            if (forward) {
                log.d("finish:play = $play, playlistItemId = ${playId.second}, itemId = $playId")
                view.gotoMain(playId.first, plItemId = playId.second, play)
                view.exit()
            } else { // return play is hidden for not connected
                playId
                    .takeIf { play && isConnected }
                    ?.let {
                        playId.first.let { itemPlaylistId -> queue.playNow(itemPlaylistId, playId.second) }
                    }
                view.exit()
            }
        } catch (t: Throwable) {
            when (t) {
                is NoDefaultPlaylistException -> // todo make a dialog or just create the playlist
                    view.error(shareStrings.errorNoDefaultPlaylist)

                else -> view.error(t.message ?: (t::class.java.simpleName + " error ... sorry"))
            }
        }
    }

    private fun selectPlaylist(data: List<PlaylistDomain>, currentPlaylistId: Identifier<GUID>) =
        (data.firstOrNull()?.id ?: currentPlaylistId) to null

    private fun selectPlaylistItem(data: List<PlaylistItemDomain>, currentPlaylistId: Identifier<GUID>) =
        chooseItem(data, currentPlaylistId)
            ?.let { it.playlistId!! to it.id }
            ?: let { currentPlaylistId to null }


    private fun chooseItem(playlistItemList: List<PlaylistItemDomain>, currentPlaylistId: Identifier<GUID>?): PlaylistItemDomain? {
        val size = playlistItemList.size
        val playlistItem: PlaylistItemDomain? = if (size == 1) {
            playlistItemList.get(0)
        } else if (size > 1) {
            val indexOfFirst = playlistItemList.indexOfFirst { it.playlistId == currentPlaylistId }
            playlistItemList.get(
                indexOfFirst.let { if (it > -1) it else 0 }
            )
        } else null
        return playlistItem
    }

}