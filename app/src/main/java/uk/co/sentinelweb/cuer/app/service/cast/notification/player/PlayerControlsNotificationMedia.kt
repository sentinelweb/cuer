package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_DISCONNECT
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_PAUSE
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_PLAY
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_SKIPB
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_SKIPF
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_STAR
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_TRACKB
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_TRACKF
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import androidx.media.app.NotificationCompat as MediaNotificationCompat

class PlayerControlsNotificationMedia constructor(
    private val service: Service,
    private val appState: CuerAppState,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
    private val launchClass: Class<out Activity>,
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
        service.startForeground(FOREGROUND_ID, buildNotification(state))
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

        val mediaSessionToken = if (state.seekEnabled) {
            (appState.mediaSession?.sessionToken
                ?: throw IllegalArgumentException("No media session ID allocated"))
        } else null
        val builder = NotificationCompat.Builder(
            service,
            appState.castNotificationChannelId ?: throw IllegalStateException("No media session")
        )
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(timeProvider.currentTimeMillis())
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionToken)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(disconnectPendingIntent)
                    .run {
                        val actionIndexes = mutableListOf(2) // #2: pause or play button
                        if (state.nextEnabled) {
                            actionIndexes.add(4) // #4: next button
                        }
                        setShowActionsInCompactView(*actionIndexes.toIntArray())

                    }
            )
            .setContentTitle(buildTitle(state))
            .setContentText(state.media?.description ?: "No description")
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)

        state.bitmap?.apply { builder.setLargeIcon(this) }

        //builder.addAction(R.drawable.ic_notif_track_b_black, "Prev", trackbPendingIntent) // #0
        builder.addAction(R.drawable.ic_notif_close_white, "Close", disconnectPendingIntent) // #0
        builder.addAction(R.drawable.ic_notif_fast_rewind_black, "-30s", skipbPendingIntent) // #1
        if (state.blocked) {
            builder.addAction(R.drawable.ic_lock_24, "Locked", contentPendingIntent)
        } else {
            when (state.playState) {
                PLAYING ->
                    builder.addAction(R.drawable.ic_notif_pause_black, "Pause", pausePendingIntent)

                PAUSED ->
                    builder.addAction(R.drawable.ic_notif_play_black, "Play", playPendingIntent)

                BUFFERING ->
                    builder.addAction(R.drawable.ic_notif_buffer_black, "Buffering", pausePendingIntent)

                ERROR ->
                    builder.addAction(R.drawable.ic_error, "Error", contentPendingIntent)

                else -> {
                    log.e("state: ${state.playState} title: ${state.media?.title}")
                    builder.addAction(R.drawable.ic_error, "Unknown", contentPendingIntent)
                }
            }
        }
        builder.addAction(R.drawable.ic_notif_fast_forward_black, "+30s", skipfPendingIntent) // #3
        if (state.nextEnabled) {
            builder.addAction(R.drawable.ic_notif_track_f_black, "Next", trackfPendingIntent) // #4
        }
        // #6 star - disabled
        // builder.addAction(R.drawable.ic_notif_unstarred_black, "Star", starPendingIntent)// #5
        return builder.build()
    }

    private fun buildTitle(state: PlayerControlsNotificationContract.State) = (state.media?.title ?: "No title")

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