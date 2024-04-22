package uk.co.sentinelweb.cuer.hub.main

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.hub.di.test.RemoteServerControllerTestModules

class TestMain() : KoinComponent {
    private val server: RemoteServerContract.Controller by inject()
    private val wifiStateProvider: WifiStateProvider by inject()
    private val log: LogWrapper by inject()

    fun setUp() {
        log.tag(this)
        GlobalContext.startKoin {
            modules(RemoteServerControllerTestModules.testModules)
        }
        wifiStateProvider.register()
    }

    fun tearDown() {
        server.destroy()
    }

    fun runServer() = runBlocking {
        server.initialise()
        delay(5000)
        while (server.isServerStarted) {
            delay(2000)
        }
    }
}

fun main() {
    TestMain().apply {
        setUp()
        runServer()
    }
}