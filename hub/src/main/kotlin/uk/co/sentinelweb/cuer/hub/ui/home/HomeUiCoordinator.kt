package uk.co.sentinelweb.cuer.hub.ui.home

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.hub.ui.filebrowser.FilesUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.player.vlc.FolderMemoryPlaylistItemLoader
import uk.co.sentinelweb.cuer.hub.ui.player.vlc.VlcPlayerUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.preferences.PreferencesUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class HomeUiCoordinator : UiCoordinator<HomeModel>, DesktopScopeComponent,
    KoinComponent {
    override val scope: Scope = desktopScopeWithSource(this)


    val remotes: RemotesUiCoordinator by inject()
    val preferencesUiCoordinator: PreferencesUiCoordinator by inject()
    val filesUiCoordinator: FilesUiCoordinator by inject { parametersOf(this) }
    private val remoteServiceManager: RemoteServerContract.Manager by inject()
    private val log: LogWrapper by inject()

    private var playerUiCoordinator: VlcPlayerUiCoordinator? = null
    private val playerItemLoader: FolderMemoryPlaylistItemLoader by inject()
    private val playlistOrchestrator: PlaylistOrchestrator by inject()
    private val coroutines: CoroutineContextProvider by inject()

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
        playerUiCoordinator = getKoin().get(parameters = { parametersOf(this@HomeUiCoordinator) })
        playerUiCoordinator?.setupPlaylistAndItem(item, playlist)
    }

    fun killPlayer() {
        playerUiCoordinator?.destroy()
        playerUiCoordinator = null
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            factory { HomeUiCoordinator() }
            scope(named<HomeUiCoordinator>()) {

            }
        }
    }
}