package uk.co.sentinelweb.cuer.hub.main

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.hub.di.Modules
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.home.home
import uk.co.sentinelweb.cuer.hub.util.remote.KeyStoreManager
import uk.co.sentinelweb.cuer.hub.util.remote.RemoteConfigFileInitialiseer

fun main() {

    startKoin {
        modules(Modules.allModules)
    }

    val koin: Koin = getKoin()

    val databaseInit = koin.get<DatabaseInitializer>()
    if (!databaseInit.isInitialized()) {
        databaseInit.initDatabase("db/default-dbinit.json")
    }

    koin.get<RemoteConfigFileInitialiseer>()
        .apply { initIfNecessary() }

    koin.get<KeyStoreManager>()
        .apply { generateKeysIfNecessary() }

    koin.get<WifiStateProvider>()
        .apply { register() }

    val homeUiCoordinator = koin.get<HomeUiCoordinator>()
        .apply { create() }

//    if (System.getProperty("os.name").toLowerCase().contains("mac")) {
//        //Application.getApplication().dockIconImage = loadSVG(resourcePath ="drawable/ic_bitcoin.svg", color= Color.Red, 64)
//        System.setProperty("apple.awt.application.name", "My Custom App Name");
//        System.setProperty("apple.laf.useScreenMenuBar", "true"); // Use the screen menu bar
//        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "My Custom App Name");
//    }

    home(homeUiCoordinator)
}
