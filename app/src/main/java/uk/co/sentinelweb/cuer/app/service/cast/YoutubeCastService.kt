package uk.co.sentinelweb.cuer.app.service.cast

import android.app.Notification
import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.roche.mdas.util.wrapper.ToastWrapper
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.scope.Scope
import org.koin.ext.getOrCreateScope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper

class YoutubeCastService : Service(), KoinComponent {

    private lateinit var scope : Scope
    private lateinit var controller : YoutubeCastServiceController
    private val toastWrapper: ToastWrapper by inject()
    private val notif: NotificationWrapper by inject()

    private lateinit var notifChannelId:String // todo move to appstate

    override fun onCreate() {
        super.onCreate()
        _instance = this
        scope = this.getOrCreateScope().apply {
            controller = get()
        }
        controller.initialise()
        // toastWrapper.showToast("Service created")
        notifChannelId = notif.createChannelId()// todo create earlier?
        startForeground(FOREGROUND_ID,notification())
    }

    // todo move to wrapper
    fun notification():Notification {
        val snoozeIntent = Intent(this, YoutubeCastService::class.java).apply {
            action = ACTION_PAUSE
            putExtra(EXTRA_NOTIFICATION_ID, 0)
        }
        val snoozePendingIntent: PendingIntent =
            PendingIntent.getService(this, 0, snoozeIntent, 0)

        return NotificationCompat.Builder(this, notifChannelId)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_player_fast_rewind_black)
            .setContentTitle("service")
            .setContentText("content")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .addAction(
                R.drawable.ic_player_pause_black, "Pause",
                snoozePendingIntent
            ).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ACTION_PAUSE == intent?.action) {
            controller.pause()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // toastWrapper.showToast("Service destroyed")
        controller.destroy()
        _instance = null
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        private const val FOREGROUND_ID = 34563
        private const val NOTIF_ID = 34564
        private const val ACTION_PAUSE = "pause"
        private var _instance : YoutubeCastService? = null

        fun instance() : YoutubeCastService? = _instance
    }


}