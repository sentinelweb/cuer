package uk.co.sentinelweb.cuer.app.service.remote

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidApplication
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract.Controller
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract.Notification
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract.Notification.External
import uk.co.sentinelweb.cuer.app.util.extension.serviceScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter

class RemoteServerService : Service(), RemoteServerContract.Service, AndroidScopeComponent {
    override val isServerStarted: Boolean
        get() = controller.isServerStarted

    override fun ping() {
        controller.ping()
    }

    override val localNode: LocalNodeDomain
        get() = controller.localNode

    override val scope: Scope by serviceScopeWithSource()
    private val controller: Controller by scope.inject()
    private val notificationWrapper: NotificationWrapper by inject()
    private val appState: CuerAppState by inject()

    private val log: LogWrapper by inject()

    override fun onCreate() {
        super.onCreate()
        log.tag(this)
        _instance = this
        log.d("Remote Service created")
        appState.remoteNotificationChannelId = notificationWrapper.createChannelId(CHANNEL_ID, CHANNEL_NAME)
        controller.initialise()
    }

    override fun onDestroy() {
        super.onDestroy()

        log.d("Service destroyed")
        controller.destroy()
        scope.close()
        _instance = null
    }

    // Note 1: intent can be null with start sticky - it might make sense to handle this and the wrapper
    // can be re-created when a null intent is received (and doesn't exist already)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // this routes media buttons to the MediaSessionCompat
        controller.handleAction(intent?.action)
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID: String = "cuer_remote_service"
        private const val CHANNEL_NAME: String = "Cuer Remote Service"

        private var _instance: RemoteServerService? = null
        fun instance(): RemoteServerService? = _instance

        val serviceModule = module {
            single<RemoteServerContract.Manager> { RemoteServerServiceManager(androidApplication()) }
            scope(named<RemoteServerService>()) {
                scoped<Controller> {
                    RemoteServerServiceController(
                        notification = get(),
                        webServer = get(),
                        coroutines = get(),
                        log = get(),
                        connectivityWrapper = get(),
                        multi = get(),
                        localRepo = get(),
                        remoteRepo = get(),
                        connectMessageMapper = get(),
                        remoteInteractor = get()
                    )
                }
                scoped<External> {
                    RemoteServerNotificationController(
                        view = get(),
                        state = get()
                    )
                }
                scoped<Notification.View> {
                    RemoteServerNotification(
                        service = get<RemoteServerService>(),
                        appState = get(),
                        timeProvider = get(),
                        log = get(),
                        res = get()
                    )
                }
                scoped { Notification.State() }
            }
            factory<RemoteDatabaseAdapter> {
                AppRemoteDatabaseAdapter(
                    playlistOrchestrator = get(),
                    playlistItemOrchestrator = get(),
                    addLinkUsecase = get()
                )
            }

        }
    }

}