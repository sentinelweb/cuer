package uk.co.sentinelweb.cuer.hub.main

import org.koin.core.context.startKoin
import uk.co.sentinelweb.cuer.hub.di.Modules
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.home.home

fun main() {
    startKoin {
        modules(Modules.allModules)
    }
    // todo create somewhere else
    home(HomeUiCoordinator().apply { create() })
}
