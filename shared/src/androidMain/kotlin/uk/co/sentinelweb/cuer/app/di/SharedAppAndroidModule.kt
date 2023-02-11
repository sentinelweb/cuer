package uk.co.sentinelweb.cuer.app.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperations
import uk.co.sentinelweb.cuer.app.service.remote.JvmMultiCastSocket
import uk.co.sentinelweb.cuer.app.service.remote.MultiCastSocketContract

object SharedAppAndroidModule {

    private val utilModule = module {
        factory { AssetOperations(androidContext()) }
    }

    private val remoteServerModule = module {
        single<MultiCastSocketContract> { JvmMultiCastSocket(MultiCastSocketContract.Config(), get(), get()) }
    }

    val modules = listOf(utilModule, remoteServerModule)
}