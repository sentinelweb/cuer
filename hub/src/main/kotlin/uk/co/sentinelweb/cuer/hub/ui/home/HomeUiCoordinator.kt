package uk.co.sentinelweb.cuer.hub.ui.home

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.hub.ui.filebrowser.FilesUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.preferences.PreferencesUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class HomeUiCoordinator : UiCoordinator<HomeModel>, DesktopScopeComponent,
    KoinComponent {
    override val scope: Scope = desktopScopeWithSource(this)


    val remotes: RemotesUiCoordinator by inject()
    val dbInit: DatabaseInitializer by inject()
    val remoteServiceManager: RemoteServerContract.Manager by inject()
    val preferencesUiCoordinator: PreferencesUiCoordinator by inject()
    val filesUiCoordinator: FilesUiCoordinator by inject()

    override var modelObservable = MutableStateFlow(HomeModel.blankModel)
        private set

    override fun create() {
        remotes.create()
        preferencesUiCoordinator.create()
        filesUiCoordinator.create()
    }

    override fun destroy() {
        remoteServiceManager.stop()
        remotes.destroy()
        preferencesUiCoordinator.destroy()
        filesUiCoordinator.destroy()
    }

    fun initDb() {
        dbInit.initDatabase("db/default-dbinit.json")
    }

    fun go(route: HomeModel.DisplayRoute) {
        modelObservable.value = modelObservable.value.copy(route = route)
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