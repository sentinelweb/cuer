package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_DISCONNECT
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_PAUSE
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_PLAY
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_SKIPB
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_SKIPF
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_STAR
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_TRACKB
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_TRACKF
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerUiMapper
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState.Connected
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ControlTarget
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*

class PlayerControlsNotificationCustom constructor(
    private val service: Service,
    private val appState: CuerAppState,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
    private val launchClass: Class<out Activity>,
    private val playerUiMapper: CastPlayerUiMapper,
    private val channelId: String?
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
        //log.d("show Notification start")
        service.startForeground(
            PlayerControlsNotificationMedia.FOREGROUND_ID,
            buildNotification(state)
        )
        //log.d("show Notification end")
    }

    override fun stopSelf() {
        service.stopSelf()
    }

    // todo try this (media notif) https://developer.android.com/training/notify-user/expanded#media-style
    // or this (custom layout) https://stackoverflow.com/questions/41888161/how-to-create-a-custom-notification-layout-in-android
    private fun buildNotification(
        state: PlayerControlsNotificationContract.State
    ): Notification {
        if (icon == -1) {
            throw IllegalStateException("Dont forget to set the icon")
        }
        val channelIdToUse = channelId
            ?: appState.castNotificationChannelId
            ?: throw IllegalStateException("No media notification channel")
        val remoteViews = RemoteViews(service.packageName, R.layout.notif_custom_media)

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

        val title = state.media?.title ?: "No title"
        val description = state.media?.description ?: "No description"

        val builder = NotificationCompat.Builder(service, channelIdToUse)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(description)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(timeProvider.currentTimeMillis())
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .setCustomBigContentView(remoteViews)
            .setSound(null)
//            .setSilent(true)
            .setChannelId(channelIdToUse)

        (state.bitmap as Bitmap?)?.apply {
            builder.setLargeIcon(this)
            remoteViews.setImageViewBitmap(R.id.notif_album_art, this)
        }

        remoteViews.setTextViewText(R.id.notif_track_title, title)
        remoteViews.setTextViewText(R.id.notif_track_description, description)

        remoteViews.setOnClickPendingIntent(R.id.notif_button_disconnect, disconnectPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.notif_button_previous_track, trackbPendingIntent)
        remoteViews.setBoolean(R.id.notif_button_previous_track, "setEnabled", state.prevEnabled)

        remoteViews.setOnClickPendingIntent(R.id.notif_button_rewind, skipbPendingIntent)

        remoteViews.setBoolean(R.id.notif_button_play_pause, "setEnabled", true)
        if (state.blocked) {
            remoteViews.setOnClickPendingIntent(R.id.notif_button_play_pause, contentPendingIntent)
            remoteViews.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_lock_24)
        } else {
            when (state.playState) {
                PLAYING -> {
                    remoteViews.setOnClickPendingIntent(R.id.notif_button_play_pause, pausePendingIntent)
                    remoteViews.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_notif_pause)
                }

                PAUSED -> {
                    remoteViews.setOnClickPendingIntent(R.id.notif_button_play_pause, playPendingIntent)
                    remoteViews.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_notif_play)
                }

                BUFFERING, VIDEO_CUED -> {
                    remoteViews.setOnClickPendingIntent(R.id.notif_button_play_pause, playPendingIntent)
                    remoteViews.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_notif_buffer)
                }

                ERROR -> {
                    remoteViews.setOnClickPendingIntent(R.id.notif_button_play_pause, contentPendingIntent)
                    remoteViews.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_notif_error)
                    remoteViews.setBoolean(R.id.notif_button_play_pause, "setEnabled", false)
                }

                else -> {
                    remoteViews.setOnClickPendingIntent(R.id.notif_button_play_pause, contentPendingIntent)
                    remoteViews.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_notif_buffer)
                }
            }
        }

        remoteViews.setOnClickPendingIntent(R.id.notif_button_forward, skipfPendingIntent)

        remoteViews.setOnClickPendingIntent(R.id.notif_button_next_track, trackfPendingIntent)
        remoteViews.setBoolean(R.id.notif_button_next_track, "setEnabled", state.nextEnabled)

        val starred = state.media?.starred ?: false
        remoteViews.setOnClickPendingIntent(R.id.notif_button_star, starPendingIntent)
        if (starred) {
            remoteViews.setImageViewResource(R.id.notif_button_star, R.drawable.ic_notif_starred)
        } else {
            remoteViews.setImageViewResource(R.id.notif_button_star, R.drawable.ic_notif_unstarred_black)
        }
        val progress = state.media
            ?.let {
                val duration = it.duration
                val positon = it.positon
                //log.d("duration: $duration, positon: $positon")
                if (duration != null && positon != null) {
                    (positon * 1000f / duration).toInt()
                } else 0
            }//?.also{log.d("progress: $it")}
            ?: 0
        remoteViews.setProgressBar(R.id.notif_progress, 1000, progress, false)

        remoteViews.setTextViewText(R.id.notif_position, playerUiMapper.formatTime(state.positionMs))
        remoteViews.setTextViewText(R.id.notif_duration, playerUiMapper.formatTime(state.durationMs))
        state.targetDetails
            .takeIf { it.target != ControlTarget.Local && it.connectionState == Connected }
            ?.let { "${it.target.toString()} - ${it.name}" }
            ?: "Local"
        when (state.targetDetails.target) {
            ControlTarget.Local -> remoteViews.setImageViewResource(R.id.notif_target_icon, R.drawable.ic_notif_iphone)
            ControlTarget.ChromeCast -> remoteViews.setImageViewResource(
                R.id.notif_target_icon,
                R.drawable.ic_notif_chromecast_connected
            )

            ControlTarget.CuerCast -> remoteViews.setImageViewResource(
                R.id.notif_target_icon,
                R.drawable.ic_notif_cuer_cast_connected
            )

            ControlTarget.FloatingWindow -> remoteViews.setImageViewResource(
                R.id.notif_target_icon,
                R.drawable.ic_notif_floating
            )
        }
        remoteViews.setTextViewText(R.id.notif_target, state.targetDetails.name)
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
