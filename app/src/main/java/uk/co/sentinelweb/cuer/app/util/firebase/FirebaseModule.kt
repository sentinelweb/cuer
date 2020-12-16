package uk.co.sentinelweb.cuer.app.util.firebase

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

object FirebaseModule {
    val fbModule = module {
        single { FirebaseWrapper(androidApplication(), get()) }
        single { FirebaseDefaultImageProvider(get()) }
    }
}