package uk.co.sentinelweb.cuer.hub.ui.home

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
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

    override var modelObservable = MutableStateFlow(HomeModel(0))
        private set

    override fun create() {
        remotes.create()
    }

    override fun destroy() {
        remoteServiceManager.stop()
        remotes.destroy()
    }

    fun initDb() {
        dbInit.initDatabase("db/default-dbinit.json")
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