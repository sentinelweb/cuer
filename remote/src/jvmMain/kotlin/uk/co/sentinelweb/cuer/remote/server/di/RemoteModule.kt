package uk.co.sentinelweb.cuer.remote.server.di

import io.ktor.util.*
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.remote.server.RemoteServer

object RemoteModule {

    val objectModule = module {
        @OptIn(KtorExperimentalAPI::class)
        factory { RemoteServer(get()) }
    }
}