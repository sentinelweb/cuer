package uk.co.sentinelweb.cuer.app.service.remote

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.remote.RemoteNotificationController.Companion.ACTION_STOP
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class RemoteNotification constructor(
    private val service: RemoteService,
    private val appState: CuerAppState,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
    private val res: ResourceWrapper
) : RemoteContract.Notification.View {

    init {
        log.tag(this)
    }

    override fun showNotification(address: String) {
        service.startForeground(
            FOREGROUND_ID,
            buildNotification(address)
        )
    }

    override fun stopSelf() {
        service.stopSelf()
    }

    private fun buildNotification(
        address: String
    ): Notification {
        val disconnectPendingIntent: PendingIntent = pendingIntent(ACTION_STOP)

        val contentIntent = Intent(service, MainActivity::class.java)
        val contentPendingIntent: PendingIntent =
            PendingIntent.getActivity(service, 0, contentIntent, FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(
            service,
            appState.remoteNotificationChannelId!!
        )
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_remote_service)
            .setContentTitle(res.getString(R.string.prefs_root_remote_service_title))
            .setContentText(address)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(timeProvider.currentTimeMillis())
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)

        builder.addAction(R.drawable.ic_notif_close_white, "Close", disconnectPendingIntent)

        return builder.build()
    }

    private fun pendingIntent(action: String): PendingIntent {
        val intent = Intent(service, RemoteService::class.java).apply {
            this.action = action
            putExtra(Notification.EXTRA_NOTIFICATION_ID, FOREGROUND_ID)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getService(service, 0, intent, FLAG_IMMUTABLE)
        return pendingIntent
    }

    companion object {
        const val FOREGROUND_ID = 34564
    }

}