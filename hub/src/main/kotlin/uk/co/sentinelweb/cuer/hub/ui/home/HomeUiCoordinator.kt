package uk.co.sentinelweb.cuer.hub.ui.home

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.hub.ui.filebrowser.FilesUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.player.vlc.VlcPlayerUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.player.vlc.VlcPlayerUiCoordinator.Companion.PREFERRED_SCREEN_DEFAULT
import uk.co.sentinelweb.cuer.hub.ui.preferences.PreferencesUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator
import uk.co.sentinelweb.cuer.remote.interact.RemotePlayerLaunchHost

@OptIn(ExperimentalCoroutinesApi::class)
class HomeUiCoordinator :
    UiCoordinator<HomeModel>,
    DesktopScopeComponent,
    RemotePlayerLaunchHost,
    KoinComponent {
    override val scope: Scope = desktopScopeWithSource(this)


    val remotes: RemotesUiCoordinator by inject()
    val preferencesUiCoordinator: PreferencesUiCoordinator by inject()
    val filesUiCoordinator: FilesUiCoordinator by inject { parametersOf(this) }
    private val remoteServiceManager: RemoteServerContract.Manager by inject()
    private val log: LogWrapper by inject()
    private val playerConfigProvider: PlayerConfigProvider by inject()

    private var playerUiCoordinator: VlcPlayerUiCoordinator? = null

    override var modelObservable = MutableStateFlow(HomeModel.blankModel)
        private set

    override fun create() {
        log.tag(this)
        remotes.create()
        preferencesUiCoordinator.create()
        filesUiCoordinator.create()
    }

    override fun destroy() {
        remoteServiceManager.stop()
        remotes.destroy()
        preferencesUiCoordinator.destroy()
        filesUiCoordinator.destroy()
        playerUiCoordinator?.destroy()
    }

    fun go(route: HomeModel.DisplayRoute) {
        modelObservable.value = modelObservable.value.copy(route = route)
    }

    fun showPlayer(item: PlaylistItemDomain, playlist: PlaylistDomain) {
        killPlayer()
        playerUiCoordinator = getKoin().get(parameters = { parametersOf(this@HomeUiCoordinator) })
        val selectedScreen = playerConfigProvider.invoke()
            .screens
            .let { if (it.size > PREFERRED_SCREEN_DEFAULT) it.get(PREFERRED_SCREEN_DEFAULT) else it.get(0) }
        playerUiCoordinator?.setupPlaylistAndItem(item, playlist, selectedScreen)
    }

    fun killPlayer() {
        playerUiCoordinator?.destroy()
        playerUiCoordinator = null
    }

    // called from the webserver
    override fun launchVideo(item: PlaylistItemDomain, screenIndex: Int?) {
        killPlayer()
        playerUiCoordinator = getKoin().get(parameters = { parametersOf(this@HomeUiCoordinator) })
        val queuePlaylist = PlaylistDomain(
            id = Identifier(QueueTemp.id, MEMORY),
            title = "Queue",
            items = listOf(item)
        )
        val index = screenIndex ?: PREFERRED_SCREEN_DEFAULT
        val selectedScreen = playerConfigProvider.invoke()
            .screens
            .let { if (it.size > index) it.get(index) else it.get(0) }
        playerUiCoordinator?.setupPlaylistAndItem(item, queuePlaylist, selectedScreen)
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            single { HomeUiCoordinator() }
            factory<RemotePlayerLaunchHost> { get<HomeUiCoordinator>() } // injects to webserver
            scope(named<HomeUiCoordinator>()) {

            }
        }
    }
}
