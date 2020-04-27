package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastService
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity

class PlayerControlsNotification constructor(
    private val service: YoutubeCastService,
    private val appState: CuerAppState
) : PlayerControlsNotificationContract.View {

    // todo try this (media notif) https://developer.android.com/training/notify-user/expanded#media-style
    // or this (custom layout) https://stackoverflow.com/questions/41888161/how-to-create-a-custom-notification-layout-in-android
    private fun buildNotification(): Notification {
        val pauseIntent = Intent(service, YoutubeCastService::class.java).apply {
            action = ACTION_PAUSE
            putExtra(Notification.EXTRA_NOTIFICATION_ID, 0)
        }
        val pausePendingIntent: PendingIntent =
            PendingIntent.getService(service, 0, pauseIntent, 0)

        val contentIntent = Intent(service, MainActivity::class.java)
        val contentPendingIntent: PendingIntent =
            PendingIntent.getActivity(service, 0, contentIntent, 0)

        return NotificationCompat.Builder(
            service,
            appState.notificationChannelId!!// todo show error
        )
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_player_fast_rewind_black)
            .setContentTitle("service")
            .setContentText("content")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(System.currentTimeMillis())
            .setContentIntent(contentPendingIntent)
            .addAction(
                R.drawable.ic_player_pause_black,
                "Pause",
                pausePendingIntent
            )
            .addAction(
                R.drawable.ic_player_pause_black,
                "Pause",
                pausePendingIntent
            )
            .addAction(
                R.drawable.ic_player_pause_black,
                "Pause",
                pausePendingIntent
            )
            .build()
    }

    override fun showNotification() {
        service.startForeground(FOREGROUND_ID, buildNotification())
    }

    companion object {
        private const val ACTION_PAUSE = "pause"
        private const val FOREGROUND_ID = 34563
    }
}