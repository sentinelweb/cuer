package uk.co.sentinelweb.cuer.remote.server.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.remote.server.RemoteServer
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract

object RemoteModule {

    val objectModule = module {
        single<RemoteWebServerContract> { RemoteServer(get(), get()) }
        // single { RemoteDatabaseAdapter(get(), get()) }
    }
}