package uk.co.sentinelweb.cuer.app.service.cast

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

class YoutubeCastService : Service(), YoutubeCastServiceContract.Service, AndroidScopeComponent {

    override val scope: Scope by serviceScopeWithSource()
    private val controller: YoutubeCastServiceContract.Controller by scope.inject()
    private val toastWrapper: ToastWrapper by inject()
    private val notificationWrapper: NotificationWrapper by inject()
    private val appState: CuerAppState by inject()
    private val log: LogWrapper by inject()

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

    // Note 1: intent can be null with start sticky - it might make sense to handle this and the wrapper
    // can be re-created when a null intent is received (and doesn't exist already)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // this routes media buttons to the MediaSessionCompat
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            MediaButtonReceiver.handleIntent(appState.mediaSession, intent)
        } else {
            controller.handleAction(intent?.action)
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID: String = "cuer_yt_service"
        private const val CHANNEL_NAME: String = "Cuer Youtube Service"

        private var _instance: YoutubeCastService? = null
        fun instance(): YoutubeCastService? = _instance
    }
}