package uk.co.sentinelweb.cuer.remote.server.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.remote.server.JvmRemoteWebServer
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract
import uk.co.sentinelweb.cuer.remote.server.multicast.JvmMultiCastSocket
import uk.co.sentinelweb.cuer.remote.server.multicast.MulticastMessageMapper

object RemoteModule {

    val objectModule = module {
        single<RemoteWebServerContract> { JvmRemoteWebServer(get(), get()) }
        single<MultiCastSocketContract> { JvmMultiCastSocket(MultiCastSocketContract.Config(), get(), get(), get(), get(), get(), get()) }
        factory { MulticastMessageMapper(get()) }
    }
}
