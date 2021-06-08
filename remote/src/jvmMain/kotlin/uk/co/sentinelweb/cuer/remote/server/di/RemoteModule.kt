package uk.co.sentinelweb.cuer.remote.server.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.remote.server.RemoteServer

object RemoteModule {

    val objectModule = module {
        factory { RemoteServer(get(), get()) }
    }
}