package uk.co.sentinelweb.cuer.app.service.remote

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.util.extension.serviceScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class RemoteService : Service(), RemoteContract.Service, AndroidScopeComponent {
    override val isServerStarted: Boolean
        get() = controller.isServerStarted

    override val address: String?
        get() = controller.address

    override val scope: Scope by serviceScopeWithSource()
    private val controller: RemoteContract.Controller by scope.inject()
    private val notificationWrapper: NotificationWrapper by inject()
    private val appState: CuerAppState by inject()
    private val coroutines: CoroutineContextProvider by inject()


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

        private var _instance: RemoteService? = null
        fun instance(): RemoteService? = _instance
    }
}