package uk.co.sentinelweb.cuer.hub.main

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.hub.di.Modules
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.home.home

fun main() {
    startKoin {
        modules(Modules.allModules)
    }
    val koin: Koin = getKoin()

    val homeUiCoordinator = koin.get<HomeUiCoordinator>()
        .apply { create() }
    val remoteServerService = koin.get<RemoteServerContract.Service>()

    home(homeUiCoordinator)
}
