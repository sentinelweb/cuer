package uk.co.sentinelweb.cuer.app

import android.app.Application
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer
import uk.co.sentinelweb.cuer.app.di.Modules
import uk.co.sentinelweb.cuer.app.exception.TerminatedWhilePlayingError
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceManager
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseWrapper
import uk.co.sentinelweb.cuer.app.util.share.SharingShortcutsManager
import uk.co.sentinelweb.cuer.app.util.wrapper.ServiceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ServiceWrapper.Companion.SERVICE_NOT_FOUND
import uk.co.sentinelweb.cuer.app.util.wrapper.StethoWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class CuerApp : Application() {

    private val stethoWrapper: StethoWrapper by inject()
    private val sharingShortcutsManager: SharingShortcutsManager by inject()
    private val databaseInit: DatabaseInitializer by inject()
    private val firebaseWrapper: FirebaseWrapper by inject()
    private val log: LogWrapper by inject()
    private val castServiceManager: YoutubeCastServiceManager by inject()
    private val serviceWrapper: ServiceWrapper by inject()

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
        firebaseWrapper.sendUnsentReports()
        setDefaultExceptionHander()
    }

    override fun onTerminate() {
        super.onTerminate()
        if (castServiceManager.isRunning()) {
            log.e(
                "App terminated while playing", TerminatedWhilePlayingError(
                    castServiceManager.get()?.let { serviceWrapper.getServiceData(it::class.java.name) } ?: SERVICE_NOT_FOUND,
                    log
                )
            )
        }
    }

    private lateinit var oldHandler: java.lang.Thread.UncaughtExceptionHandler

    fun setDefaultExceptionHander() {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
            firebaseWrapper.logMessage("Thread Name: ${paramThread.name} state:${paramThread.state} group:${paramThread.threadGroup?.name}")
            log.e("FATAL Global Exception", paramThrowable)
            if (oldHandler != null) {
                oldHandler.uncaughtException(paramThread, paramThrowable)
            } else {
                System.exit(0)
            }
        }
    }
}