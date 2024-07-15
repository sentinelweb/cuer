package uk.co.sentinelweb.cuer.core.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProvider
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProviderAndroid

object AndroidDomainModule {
    val objectModule = module {
        factory<PlayerConfigProvider> { PlayerConfigProviderAndroid(androidApplication()) }
    }
}