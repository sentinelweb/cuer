package uk.co.sentinelweb.cuer.app

import android.app.Application
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.di.Modules
import uk.co.sentinelweb.cuer.app.util.share.SharingShortcutsManager
import uk.co.sentinelweb.cuer.app.util.wrapper.StethoWrapper

class CuerApp : Application() {

    private val stethoWrapper:StethoWrapper by inject()
    private val sharingShortcutsManager: SharingShortcutsManager by inject()
    private val databaseInit: DatabaseInitializer by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // use AndroidLogger as Koin Logger - default Level.INFO
            androidLogger()

            // use the Android context given there
            androidContext(this@CuerApp)

            modules(Modules.allModules)
        }

        stethoWrapper.init()
        sharingShortcutsManager.apply {
            removeAllDirectShareTargets(this@CuerApp)
            pushDirectShareTargets(this@CuerApp)
        }

        databaseInit.initDatabase()
    }
}