package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media.session.MediaButtonReceiver
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.util.extension.serviceScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class FloatingPlayerService : Service(), FloatingPlayerContract.Service, AndroidScopeComponent {

    override val scope: Scope by serviceScopeWithSource()
    private val controller: FloatingPlayerContract.Controller by scope.inject()
    private val toastWrapper: ToastWrapper by inject()
    private val notificationWrapper: NotificationWrapper by inject()
    private val appState: CuerAppState by inject()
    private val log: LogWrapper by inject()

    override val external: FloatingPlayerContract.External
        get() = controller.external

    override fun onCreate() {
        super.onCreate()
        log.tag(this)
        _instance = this
        log.d("Service created")
        appState.castNotificationChannelId = notificationWrapper.createChannelId(CHANNEL_ID, CHANNEL_NAME)
        controller.initialise()
    }

    override fun onDestroy() {
        super.onDestroy()
        log.d("Service destroyed")
        controller.destroy()
        scope.close()
        _instance = null
    }

    // media events are configured in manifest
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // this routes media buttons to the MediaSessionCompat
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            MediaButtonReceiver.handleIntent(appState.mediaSession, intent)
        } else {
            intent?.apply { controller.handleAction(intent) }
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    fun cleanup() {

    }

    companion object {
        const val ACTION_INIT: String = "init"
        const val ACTION_PLAY_ITEM: String = "playitem"
        private const val CHANNEL_ID: String = "cuer_floating_service"
        private const val CHANNEL_NAME: String = "Cuer floating Service"

        private var _instance: FloatingPlayerService? = null
        fun instance(): FloatingPlayerService? = _instance
    }
}