package uk.co.sentinelweb.cuer.app

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.debug.DebugProbes
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.di.Modules
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseWrapper
import uk.co.sentinelweb.cuer.app.util.share.SharingShortcutsManager
import uk.co.sentinelweb.cuer.app.util.wrapper.StethoWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class CuerApp : Application() {

    private val stethoWrapper:StethoWrapper by inject()
    private val sharingShortcutsManager: SharingShortcutsManager by inject()
    private val databaseInit: DatabaseInitializer by inject()
    private val firebaseWrapper: FirebaseWrapper by inject()
    private val log: LogWrapper by inject()

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // use AndroidLogger as Koin Logger - default Level.INFO
            androidLogger(level = Level.ERROR)

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

        firebaseWrapper.init()

        setDefaultExceptionHander()
    }

    val oldHandler = Thread.getDefaultUncaughtExceptionHandler()

    @ExperimentalCoroutinesApi
    fun setDefaultExceptionHander() {
        if (BuildConfig.DEBUG) {
            DebugProbes.enableCreationStackTraces
        }
        Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
            DebugProbes.dumpCoroutines(System.err)
            if (oldHandler != null) oldHandler.uncaughtException(
                paramThread,
                paramThrowable
            )
            else System.exit(2) //Prevents the service/app from freezing
        }
    }
}