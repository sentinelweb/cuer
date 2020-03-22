package uk.co.sentinelweb.cuer.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import uk.co.sentinelweb.cuer.app.di.Modules

class CuerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // use AndroidLogger as Koin Logger - default Level.INFO
            androidLogger()

            // use the Android context given there
            androidContext(this@CuerApp)

            modules(Modules.allModules)
        }
    }
}