package uk.co.sentinelweb.cuer.hub.ui.home

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.QueueTemp
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.hub.ui.filebrowser.FilesUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.home.HomeContract.HomeModel.DisplayRoute.Folders
import uk.co.sentinelweb.cuer.hub.ui.home.HomeContract.Label
import uk.co.sentinelweb.cuer.hub.ui.local.LocalUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.player.vlc.VlcPlayerUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.player.vlc.VlcPlayerUiCoordinator.Companion.PREFERRED_SCREEN_DEFAULT
import uk.co.sentinelweb.cuer.hub.ui.preferences.PreferencesUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator
import uk.co.sentinelweb.cuer.remote.interact.PlayerLaunchHost
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract

class HomeUiCoordinator(
    private val coroutines: CoroutineContextProvider
) :
    UiCoordinator<HomeContract.HomeModel>,
    DesktopScopeComponent,
    PlayerLaunchHost,
    PlayerContract.LocalStatus,
    KoinComponent {
    override val scope: Scope = desktopScopeWithSource(this)

    val remotesCoordinator: RemotesUiCoordinator by inject() { parametersOf(this) }
    val preferencesUiCoordinator: PreferencesUiCoordinator by inject()
    val filesUiCoordinator: FilesUiCoordinator by inject { parametersOf(this) }
    val localCoordinator:LocalUiCoordinator by inject()

    private val remoteServiceManager: RemoteServerContract.Manager by inject()
    private val log: LogWrapper by inject()
    private val playerConfigProvider: PlayerConfigProvider by inject()

    private var _playerUiCoordinator: VlcPlayerUiCoordinator? = null
    val playerUiCoordinator: VlcPlayerUiCoordinator?
        get() = _playerUiCoordinator

    override var modelObservable = MutableStateFlow(HomeContract.HomeModel.Initial)
        private set

    private val _label = MutableSharedFlow<Label>()
    val label: SharedFlow<Label> = _label

    override fun create() {
        log.tag(this)
        remotesCoordinator.create()
        preferencesUiCoordinator.create()
        filesUiCoordinator.create()
        localCoordinator.create()
    }

    override fun destroy() {
        remoteServiceManager.stop()
        remotesCoordinator.destroy()
        preferencesUiCoordinator.destroy()
        filesUiCoordinator.destroy()
        _playerUiCoordinator?.destroy()
        localCoordinator.destroy()
        scope.close()
    }

    fun go(route: HomeContract.HomeModel.DisplayRoute) {
        when (route) {
            is Folders -> filesUiCoordinator.openNode(route.node)
            else -> Unit
        }
        modelObservable.value = modelObservable.value.copy(route = route)
    }

//    fun showPlayer(item: PlaylistItemDomain, playlist: PlaylistDomain) {
//        killPlayer()
//        _playerUiCoordinator = getKoin().get(parameters = { parametersOf(this@HomeUiCoordinator) })
//        val selectedScreen = playerConfigProvider.invoke()
//            .screens
//            .let { if (it.size > PREFERRED_SCREEN_DEFAULT) it.get(PREFERRED_SCREEN_DEFAULT) else it.get(0) }
//        _playerUiCoordinator?.setupPlaylistAndItem(item, playlist, selectedScreen)
//        modelObservable.value = modelObservable.value.copy(showPlayer = true)
//    }

    fun killPlayer() {
        modelObservable.value = modelObservable.value.copy(showPlayer = false)
        _playerUiCoordinator?.destroy()
        _playerUiCoordinator = null
    }

    override fun launchPlayerVideo(item: PlaylistItemDomain, screenIndex: Int?) {
        killPlayer()
        log.d("------------ launchPlayerVideo: item: ${item.media.title} ----------------------------------")
        _playerUiCoordinator = getKoin().get(parameters = { parametersOf(this@HomeUiCoordinator) })
        val queuePlaylist = PlaylistDomain(
            id = Identifier(QueueTemp.id, MEMORY),
            title = "Queue",
            items = listOf(item)
        )
        val index = screenIndex ?: PREFERRED_SCREEN_DEFAULT
        val selectedScreen = playerConfigProvider.invoke()
            .screens
            .let { if (it.size > index) it.get(index) else it.get(0) }
        _playerUiCoordinator?.setupPlaylistAndItem(item, queuePlaylist, selectedScreen)
        modelObservable.value = modelObservable.value.copy(showPlayer = true)
    }

    override fun playerStatus(): PlayerSessionContract.PlayerStatusMessage {
        TODO("Not yet implemented")
    }

    override fun isPlayerActive(): Boolean = _playerUiCoordinator != null

    fun showError(message: String) {
        log.d("showError: $message")
        coroutines.mainScope.launch {
            _label.emit(Label.ErrorMessage(message))
        }
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            single { HomeUiCoordinator(get()) }
            factory<PlayerLaunchHost> { get<HomeUiCoordinator>() }
            factory<PlayerContract.LocalStatus> { get<HomeUiCoordinator>() }
            scope(named<HomeUiCoordinator>()) {

            }
        }
    }

}
