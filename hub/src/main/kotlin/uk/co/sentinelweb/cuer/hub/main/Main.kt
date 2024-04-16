package uk.co.sentinelweb.cuer.hub.main

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.hub.di.Modules
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.home.home

fun main() {
    startKoin {
        modules(Modules.allModules)
    }
    val koin: Koin = getKoin()

    // db test
    val databaseInit = koin.get<DatabaseInitializer>()
    if (!databaseInit.isInitialized()) {
        databaseInit.initDatabase("db/default-dbinit.json")
    }

    koin.get<WifiStateProvider>()
        .apply { register() }

    val homeUiCoordinator = koin.get<HomeUiCoordinator>()
        .apply { create() }

    home(homeUiCoordinator)
}
