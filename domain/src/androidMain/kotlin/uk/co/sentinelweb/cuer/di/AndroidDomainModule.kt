package uk.co.sentinelweb.cuer.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProvider
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProviderAndroid

object AndroidDomainModule {
    private val coreModule = module {
        factory<PlayerConfigProvider> { PlayerConfigProviderAndroid(androidApplication()) }
    }

    val allModules = listOf(coreModule)
}