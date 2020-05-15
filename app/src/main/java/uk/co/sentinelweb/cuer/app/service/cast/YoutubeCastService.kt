package uk.co.sentinelweb.cuer.app.service.cast

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.media.session.MediaButtonReceiver
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.scope.Scope
import org.koin.ext.getOrCreateScope
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper

class YoutubeCastService : Service(), KoinComponent {

    private lateinit var scope: Scope
    private lateinit var controller: YoutubeCastServiceController
    private val toastWrapper: ToastWrapper by inject()
    private val notificationWrapper: NotificationWrapper by inject()
    private val appState: CuerAppState by inject()

    override fun onCreate() {
        super.onCreate()
        _instance = this
        scope = this.getOrCreateScope().apply {
            controller = get()
        }
        // toastWrapper.showToast("Service created")
        appState.castNotificationChannelId = notificationWrapper.createChannelId()
        controller.initialise()
    }

    override fun onDestroy() {
        super.onDestroy()
        // toastWrapper.showToast("Service destroyed")
        controller.destroy()
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
        private var _instance: YoutubeCastService? = null

        fun instance(): YoutubeCastService? = _instance
    }
}