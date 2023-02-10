package uk.co.sentinelweb.cuer.remote.server.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.remote.server.RemoteServer
import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter

object RemoteModule {

    val objectModule = module {
        single { RemoteServer(get(), get()) }
        // single { RemoteDatabaseAdapter(get(), get()) }
    }
}