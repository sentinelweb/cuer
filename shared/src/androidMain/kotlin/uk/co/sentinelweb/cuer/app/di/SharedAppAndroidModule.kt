package uk.co.sentinelweb.cuer.app.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperations

object SharedAppAndroidModule {

    private val utilModule = module {
        factory { AssetOperations(androidContext()) }
    }

    val modules = listOf(utilModule)
}