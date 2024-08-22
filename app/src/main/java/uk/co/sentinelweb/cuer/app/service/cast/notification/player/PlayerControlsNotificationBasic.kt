package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.cast.CastService
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_DISCONNECT
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_PAUSE
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_PLAY
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_SKIPB
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_SKIPF
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_STAR
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_TRACKB
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_TRACKF
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*

class PlayerControlsNotificationBasic constructor(
    private val service: CastService,
    private val appState: CuerAppState,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
    private val launchClass: Class<out Activity>,
    private val channelId: String?,
) : PlayerControlsNotificationContract.View {

    init {
        log.tag(this)
    }

    @DrawableRes
    private var icon: Int = -1

    override fun setIcon(@DrawableRes icon: Int) {
        this.icon = icon
    }

    override fun showNotification(
        state: PlayerControlsNotificationContract.State
    ) {
        service.startForeground(
            PlayerControlsNotificationMedia.FOREGROUND_ID,
            buildNotification(state)
        )
    }

    override fun stopSelf() {
        service.stopSelf()
    }

    private fun buildNotification(
        state: PlayerControlsNotificationContract.State
    ): Notification {
        if (icon == -1) {
            throw IllegalStateException("Dont forget to set the icon")
        }
        val channelIdToUse = channelId
            ?: appState.castNotificationChannelId
            ?: throw IllegalStateException("No media notification channel")

        val pausePendingIntent: PendingIntent = pendingIntent(ACTION_PAUSE)
        val playPendingIntent: PendingIntent = pendingIntent(ACTION_PLAY)
        val skipfPendingIntent: PendingIntent = pendingIntent(ACTION_SKIPF)
        val skipbPendingIntent: PendingIntent = pendingIntent(ACTION_SKIPB)
        val trackfPendingIntent: PendingIntent = pendingIntent(ACTION_TRACKF)
        val trackbPendingIntent: PendingIntent = pendingIntent(ACTION_TRACKB)
        val disconnectPendingIntent: PendingIntent = pendingIntent(ACTION_DISCONNECT)
        val starPendingIntent: PendingIntent = pendingIntent(ACTION_STAR)

        val contentIntent = Intent(service, launchClass)
        val contentPendingIntent: PendingIntent =
            PendingIntent.getActivity(service, 0, contentIntent, FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(service, channelIdToUse)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(icon)
            .setContentTitle(state.media?.title ?: "No title")
            .setContentText(state.media?.description ?: "No description")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(timeProvider.currentTimeMillis())
            .setOngoing(true)
//            .setSilent(true)
            .setSound(null)
            .setContentIntent(contentPendingIntent)

        (state.bitmap as Bitmap?)?.apply { builder.setLargeIcon(this) }

        builder.addAction(R.drawable.ic_notif_close, "Close", disconnectPendingIntent) // #0
        builder.addAction(R.drawable.ic_notif_fast_rewind, "-30s", skipbPendingIntent)
        if (state.blocked) {
            builder.addAction(R.drawable.ic_lock_24, "Locked", contentPendingIntent)
        } else {
            when (state.playState) {
                PLAYING ->
                    builder.addAction(R.drawable.ic_notif_pause, "Pause", pausePendingIntent)

                PAUSED ->
                    builder.addAction(R.drawable.ic_notif_play, "Play", playPendingIntent)

                BUFFERING ->
                    builder.addAction(R.drawable.ic_notif_buffer, "Buffering", playPendingIntent)

                ERROR ->
                    builder.addAction(R.drawable.ic_error, "Error", contentPendingIntent)

                else -> {
                    log.e("state: ${state.playState} title: ${state.media?.title}")
                    builder.addAction(R.drawable.ic_error, "Unknown", contentPendingIntent)
                }
            }
        }
        builder.addAction(R.drawable.ic_notif_fast_forward, "+30s", skipfPendingIntent)
        if (state.nextEnabled) {
            builder.addAction(R.drawable.ic_notif_track_next, "Next", trackfPendingIntent) // #4
        }
        return builder.build()
    }

    private fun pendingIntent(action: String): PendingIntent {
        val intent = Intent(service, service::class.java).apply {
            this.action = action
            putExtra(Notification.EXTRA_NOTIFICATION_ID, FOREGROUND_ID)
        }
        return PendingIntent.getService(service, 0, intent, FLAG_IMMUTABLE)
    }

    companion object {
        val FOREGROUND_ID = if (BuildConfig.DEBUG) 34563 else 34564
    }
}
