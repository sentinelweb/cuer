package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastService
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationPresenter.Companion.ACTION_PAUSE
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationPresenter.Companion.ACTION_PLAY
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationPresenter.Companion.ACTION_SKIPB
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationPresenter.Companion.ACTION_SKIPF
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*

class PlayerControlsNotificationBasic constructor(
    private val service: YoutubeCastService,
    private val appState: CuerAppState
) : PlayerControlsNotificationContract.View {

    // todo try this (media notif) https://developer.android.com/training/notify-user/expanded#media-style
    // or this (custom layout) https://stackoverflow.com/questions/41888161/how-to-create-a-custom-notification-layout-in-android
    private fun buildNotification(state: PlayerStateDomain): Notification {
        val pausePendingIntent: PendingIntent = pendingIntent(ACTION_PAUSE)
        val playPendingIntent: PendingIntent = pendingIntent(ACTION_PLAY)
        val skipfPendingIntent: PendingIntent = pendingIntent(ACTION_SKIPF)
        val skipbPendingIntent: PendingIntent = pendingIntent(ACTION_SKIPB)
//        val trackfPendingIntent: PendingIntent = pendingIntent(ACTION_TRACKF)
//        val trackbPendingIntent: PendingIntent = pendingIntent(ACTION_TRACKB)

        val contentIntent = Intent(service, MainActivity::class.java)
        val contentPendingIntent: PendingIntent =
            PendingIntent.getActivity(service, 0, contentIntent, 0)

        val builder = NotificationCompat.Builder(
            service,
            appState.castNotificationChannelId!! // todo show error
        )
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_notif_status_cast_conn_white)
            .setContentTitle("service")
            .setContentText("content")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(System.currentTimeMillis())
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)

        builder.addAction(R.drawable.ic_player_pause_black, "+30s", skipfPendingIntent)

        when (state) {
            PLAYING ->
                builder.addAction(R.drawable.ic_notif_pause_black, "Pause", pausePendingIntent)
            PAUSED ->
                builder.addAction(R.drawable.ic_notif_play_black, "Play", playPendingIntent)
            BUFFERING ->
                builder.addAction(R.drawable.ic_notif_buffer_black, "Buffering", playPendingIntent)
            ERROR ->
                builder.addAction(R.drawable.ic_notif_buffer_black, "Error", contentPendingIntent)
            else -> Unit
        }
        builder.addAction(R.drawable.ic_player_pause_black, "-30s", skipbPendingIntent)
        return builder.build()
    }

    private fun pendingIntent(action: String): PendingIntent {
        val intent = Intent(service, YoutubeCastService::class.java).apply {
            this.action = action
            putExtra(Notification.EXTRA_NOTIFICATION_ID, FOREGROUND_ID)
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getService(service, 0, intent, 0)
        return pendingIntent
    }

    override fun showNotification(state: PlayerStateDomain) {
        service.startForeground(FOREGROUND_ID, buildNotification(state))
    }

    companion object {

        const val FOREGROUND_ID = 34563
    }
}