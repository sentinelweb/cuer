package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_DELETE
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_DISCONNECT
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_PAUSE
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_PLAY
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_SKIPB
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_SKIPF
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_TRACKF
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
    private val channelId: String?,
) : PlayerControlsNotificationContract.View {


    @DrawableRes
    private var icon: Int = -1
    private var startedForeground = false
    private val notificationManager: NotificationManager

    init {
        log.tag(this)
        notificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun setIcon(@DrawableRes icon: Int) {
        this.icon = icon
    }

    override fun showNotification(
        state: PlayerControlsNotificationContract.State
    ) {
        val builtNotification = buildNotification(state)
        if (!startedForeground) {
            service.startForeground(FOREGROUND_ID, builtNotification)
            startedForeground = true
        } else {
            notificationManager.notify(FOREGROUND_ID, builtNotification)
        }
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

        // try using the mediacompat actions here
        val pausePendingIntent: PendingIntent = pendingIntent(ACTION_PAUSE)
        val playPendingIntent: PendingIntent = pendingIntent(ACTION_PLAY)
        val skipfPendingIntent: PendingIntent = pendingIntent(ACTION_SKIPF)
        val skipbPendingIntent: PendingIntent = pendingIntent(ACTION_SKIPB)
        val trackfPendingIntent: PendingIntent = pendingIntent(ACTION_TRACKF)
        // val trackbPendingIntent: PendingIntent = pendingIntent(ACTION_TRACKB)
        val disconnectPendingIntent: PendingIntent = pendingIntent(ACTION_DISCONNECT)
        // val starPendingIntent: PendingIntent = pendingIntent(ACTION_STAR)
        val deletePendingIntent: PendingIntent = pendingIntent(ACTION_DELETE)

        val contentIntent = Intent(service, launchClass)
        val contentPendingIntent: PendingIntent =
            PendingIntent.getActivity(service, 0, contentIntent, FLAG_IMMUTABLE)

        val mediaSessionToken = //if (state.seekEnabled) {
            (appState.mediaSession?.sessionToken
                ?: throw IllegalArgumentException("No media session ID allocated"))
        //} else null
        val builder = NotificationCompat.Builder(service, channelIdToUse)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(timeProvider.currentTimeMillis())
            .setContentTitle(buildTitle(state))
            .setContentText(state.item?.media?.description)
            .setOngoing(false)
            .setContentIntent(contentPendingIntent)
            .setChannelId(channelIdToUse)
            .setDeleteIntent(deletePendingIntent)

        (state.bitmap as? Bitmap?)?.apply { builder.setLargeIcon(this) }

        //builder.addAction(R.drawable.ic_notif_track_b_black, "Prev", trackbPendingIntent) // #0
        builder.addAction(R.drawable.ic_notif_close, "Close", disconnectPendingIntent) // #0
        builder.addAction(R.drawable.ic_notif_fast_rewind, "<<", skipbPendingIntent) // #1
        if (state.blocked) {
            builder.addAction(R.drawable.ic_lock_24, "Locked", contentPendingIntent)
        } else {
            when (state.playState) {
                PLAYING ->
                    builder.addAction(R.drawable.ic_notif_pause, "Pause", pausePendingIntent)

                PAUSED ->
                    builder.addAction(R.drawable.ic_notif_play, "Play", playPendingIntent)

                BUFFERING ->
                    builder.addAction(R.drawable.ic_notif_buffer, "Buffering", pausePendingIntent)

                ERROR ->
                    builder.addAction(R.drawable.ic_error, "Error", contentPendingIntent)

                else -> {
                    log.e("state: ${state.playState} title: ${state.item?.media?.title}")
                    builder.addAction(R.drawable.ic_error, "Unknown", contentPendingIntent)
                }
            }
        }
        builder.addAction(R.drawable.ic_notif_fast_forward, ">>", skipfPendingIntent) // #3
        if (state.nextEnabled) {
            builder.addAction(R.drawable.ic_notif_track_next, "Next", trackfPendingIntent) // #4
        }
        // #6 star - disabled
        // builder.addAction(R.drawable.ic_notif_unstarred_black, "Star", starPendingIntent)// #5

        builder.setStyle(
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
        return builder.build()
    }

    override fun onDeleteAction() {
        notificationManager.cancel(FOREGROUND_ID)
        startedForeground = false
    }

    private fun buildTitle(state: PlayerControlsNotificationContract.State) = (state.item?.media?.title ?: "No title")

    private fun pendingIntent(action: String): PendingIntent {
        val intent = Intent(service, service::class.java).apply {
            this.action = action
            putExtra(Notification.EXTRA_NOTIFICATION_ID, FOREGROUND_ID)
        }
        return PendingIntent.getService(service, 0, intent, FLAG_IMMUTABLE)
    }

    companion object {
        val FOREGROUND_ID = if (BuildConfig.DEBUG) 34565 else 34566
    }
}
