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
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.receiver.ScreenStateReceiver
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceManager
import uk.co.sentinelweb.cuer.app.util.chromecast.ChromeCastSessionListener
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseWrapper
import uk.co.sentinelweb.cuer.app.util.permission.NotificationChannelCreator
import uk.co.sentinelweb.cuer.app.util.share.SharingShortcutsManager
import uk.co.sentinelweb.cuer.app.util.wrapper.ServiceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ServiceWrapper.Companion.SERVICE_NOT_FOUND
import uk.co.sentinelweb.cuer.app.util.wrapper.StethoWrapper
import uk.co.sentinelweb.cuer.app.work.WorkManagerInteractor
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import kotlin.time.ExperimentalTime

@ExperimentalTime
class CuerApp : Application() {

    private val stethoWrapper: StethoWrapper by inject()
    private val sharingShortcutsManager: SharingShortcutsManager by inject()
    private val databaseInit: DatabaseInitializer by inject()
    private val firebaseWrapper: FirebaseWrapper by inject()
    private val log: LogWrapper by inject()
    private val castServiceManager: CastServiceManager by inject()
    private val serviceWrapper: ServiceWrapper by inject()
    private val castSessionListener: ChromeCastSessionListener by inject()
    private val queue: QueueMediatorContract.Producer by inject()
    private val screenStateReceiver: ScreenStateReceiver by inject()
    private val wifiReceiver: WifiStateProvider by inject()
    private val workManagerInteractor: WorkManagerInteractor by inject()
    private val notificationChannelCreator: NotificationChannelCreator by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // use AndroidLogger as Koin Logger - default Level.INFO
            androidLogger(level = Level.ERROR)

            // use the Android context given there
            androidContext(this@CuerApp)

            modules(Modules.allModules)
        }
        castSessionListener.listen()
        stethoWrapper.init()
        sharingShortcutsManager.apply {
            removeAllDirectShareTargets(this@CuerApp)
            pushDirectShareTargets(this@CuerApp)
        }

        if (!databaseInit.isInitialized()) {
            databaseInit.initDatabase("default-dbinit.json")
        }

        firebaseWrapper.init()
        firebaseWrapper.sendUnsentReports()
        setDefaultExceptionHander()
        screenStateReceiver.register(this)
        wifiReceiver.register()
        workManagerInteractor.startUpcomingVideosChecker(this)

        notificationChannelCreator.create()
    }

    override fun onTerminate() {
        super.onTerminate()
        castSessionListener.destroy()
        if (castServiceManager.isRunning()) {
            log.e(
                "App terminated while playing", TerminatedWhilePlayingError(
                    castServiceManager.get()
                        ?.let { serviceWrapper.getServiceData(it::class.java.name) }
                        ?: SERVICE_NOT_FOUND,
                    log
                )
            )
        }
        queue.destroy()
        screenStateReceiver.unregister(this) // registered in module
        wifiReceiver.unregister()
    }

    private var oldHandler: Thread.UncaughtExceptionHandler? = null

    fun setDefaultExceptionHander() {
        oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
            firebaseWrapper.logMessage("Thread Name: ${paramThread.name} state:${paramThread.state} group:${paramThread.threadGroup?.name}")
            log.e("FATAL Global Exception", paramThrowable)
            oldHandler
                ?.uncaughtException(paramThread, paramThrowable)
                ?: run { System.exit(0) }
        }
    }
}
