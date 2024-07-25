package uk.co.sentinelweb.cuer.remote.server.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.remote.server.AvailableMessageMapper
import uk.co.sentinelweb.cuer.remote.server.JvmRemoteWebServer
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract.Companion.MULTICAST_PORT_DEBUG_DEF
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract.Companion.MULTICAST_PORT_DEF
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract
import uk.co.sentinelweb.cuer.remote.server.multicast.JvmMultiCastSocket

object RemoteModule {

    val objectModule = module {
        single<RemoteWebServerContract> { JvmRemoteWebServer(get(), get(), get()) }
        single<MultiCastSocketContract> {
            val buildConfigDomain: BuildConfigDomain = get()
            val multiPort = if (buildConfigDomain.isDebug) MULTICAST_PORT_DEBUG_DEF else MULTICAST_PORT_DEF
            val config = MultiCastSocketContract.Config(multicastPort = multiPort)
            JvmMultiCastSocket(
                config = config,
                log = get(),
                localRepository = get(),
                availableMessageMapper = get(),
                availableMessageHandler = get(),
                coroutines = get()
            )
        }
        factory { AvailableMessageMapper(get()) }
    }
}
