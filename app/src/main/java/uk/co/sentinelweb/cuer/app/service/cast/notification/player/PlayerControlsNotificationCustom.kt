package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.EXTRA_ITEM_ID
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_NONE
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_STAR
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_VOL_DOWN
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_VOL_MUTE
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_VOL_UP
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerUiMapper
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState.Connected
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ControlTarget
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.ext.serialise

class PlayerControlsNotificationCustom constructor(
    private val service: Service,
    private val appState: CuerAppState,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
    private val launchClass: Class<out Activity>,
    private val playerUiMapper: CastPlayerUiMapper,
    private val channelId: String?,
    private val showVolumeControls: Boolean,
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

        val remoteView = RemoteViews(service.packageName, R.layout.notif_custom_media)

        val contentIntent = Intent(service, launchClass)
        val contentPendingIntent: PendingIntent =
            PendingIntent.getActivity(service, 0, contentIntent, FLAG_IMMUTABLE)
        val title = state.item?.media?.title ?: "No title"
        val description = state.targetDetails.name
            ?: state.item?.media?.description
            ?: "No description"

        val builder = NotificationCompat.Builder(service, channelIdToUse)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(description)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(timeProvider.currentTimeMillis())
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .setCustomBigContentView(remoteView)
            .setSound(null)
            .setSilent(true)
            .setChannelId(channelIdToUse)

        (state.bitmap as Bitmap?)?.apply {
            builder.setLargeIcon(this)
            remoteView.setImageViewBitmap(R.id.notif_album_art, this)
        }

        val blankPendingIntent = pendingIntent(ACTION_NONE)
        remoteView.setOnClickPendingIntent(R.id.notif_root, blankPendingIntent)

        remoteView.setOnClickPendingIntent(R.id.notif_album_art, contentPendingIntent)
        remoteView.setOnClickPendingIntent(R.id.notif_track_title, contentPendingIntent)
        remoteView.setOnClickPendingIntent(R.id.notif_target_continer, contentPendingIntent)
        remoteView.setOnClickPendingIntent(R.id.notif_progress, contentPendingIntent)

        remoteView.setTextViewText(R.id.notif_track_title, title)
        remoteView.setTextViewText(R.id.notif_track_description, description)

        // previous track
        val trackbPendingIntent: PendingIntent = pendingIntent(CastServiceContract.ACTION_TRACKB)
        remoteView.setOnClickPendingIntent(R.id.notif_button_previous_track, trackbPendingIntent)
        remoteView.setBoolean(R.id.notif_button_previous_track, "setEnabled", state.prevEnabled)

        // skip back
        val skipbPendingIntent: PendingIntent = pendingIntent(CastServiceContract.ACTION_SKIPB)
        remoteView.setOnClickPendingIntent(R.id.notif_button_rewind, skipbPendingIntent)
        remoteView.setBoolean(R.id.notif_button_play_pause, "setEnabled", true)

        // play/pause button
        val playPendingIntent: PendingIntent = pendingIntent(CastServiceContract.ACTION_PLAY)
        if (state.blocked) {
            remoteView.setOnClickPendingIntent(R.id.notif_button_play_pause, contentPendingIntent)
            remoteView.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_lock_24)
        } else {
            when (state.playState) {
                PLAYING -> {
                    val pausePendingIntent: PendingIntent = pendingIntent(CastServiceContract.ACTION_PAUSE)
                    remoteView.setOnClickPendingIntent(R.id.notif_button_play_pause, pausePendingIntent)
                    remoteView.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_notif_pause)
                }

                PAUSED -> {
                    remoteView.setOnClickPendingIntent(R.id.notif_button_play_pause, playPendingIntent)
                    remoteView.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_notif_play)
                }

                BUFFERING, VIDEO_CUED -> {
                    remoteView.setOnClickPendingIntent(R.id.notif_button_play_pause, playPendingIntent)
                    remoteView.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_notif_buffer)
                }

                ERROR -> {
                    remoteView.setOnClickPendingIntent(R.id.notif_button_play_pause, contentPendingIntent)
                    remoteView.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_notif_error)
                    remoteView.setBoolean(R.id.notif_button_play_pause, "setEnabled", false)
                }

                else -> {
                    remoteView.setOnClickPendingIntent(R.id.notif_button_play_pause, contentPendingIntent)
                    remoteView.setImageViewResource(R.id.notif_button_play_pause, R.drawable.ic_notif_buffer)
                }
            }
        }

        // skip forward
        val skipfPendingIntent: PendingIntent = pendingIntent(CastServiceContract.ACTION_SKIPF)
        remoteView.setOnClickPendingIntent(R.id.notif_button_forward, skipfPendingIntent)

        // next track
        val trackfPendingIntent: PendingIntent = pendingIntent(CastServiceContract.ACTION_TRACKF)
        remoteView.setOnClickPendingIntent(R.id.notif_button_next_track, trackfPendingIntent)
        remoteView.setBoolean(R.id.notif_button_next_track, "setEnabled", state.nextEnabled)

        // star button
        val starred = state.item?.media?.starred == true
        val xtras = state.item?.id?.let { mapOf(EXTRA_ITEM_ID to it.serialise()) }
        val starPendingIntent: PendingIntent = pendingIntent(ACTION_STAR, xtras)
        remoteView.setOnClickPendingIntent(R.id.notif_button_star, starPendingIntent)
        if (starred) {
            remoteView.setImageViewResource(R.id.notif_button_star, R.drawable.ic_notif_starred)
        } else {
            remoteView.setImageViewResource(R.id.notif_button_star, R.drawable.ic_notif_unstarred_black)
        }

        //progress bar
        val progress = state.item
            ?.let {
                val duration = it.media.duration
                val positon = it.media.positon
                if (duration != null && positon != null) {
                    (positon * 1000f / duration).toInt()
                } else 0
            }
            ?: 0
        remoteView.setProgressBar(R.id.notif_progress, 1000, progress, false)

        // position / duration
        remoteView.setTextViewText(R.id.notif_position, playerUiMapper.formatTime(state.positionMs))
        remoteView.setTextViewText(R.id.notif_duration, playerUiMapper.formatTime(state.durationMs))

        // target item
        state.targetDetails
            .takeIf { it.target != ControlTarget.Local && it.connectionState == Connected }
            ?.let { "${it.target.toString()} - ${it.name}" }
            ?: "Local"
        when (state.targetDetails.target) {
            ControlTarget.Local -> remoteView.setImageViewResource(R.id.notif_target_icon, R.drawable.ic_notif_iphone)
            ControlTarget.ChromeCast -> remoteView.setImageViewResource(
                R.id.notif_target_icon,
                R.drawable.ic_notif_chromecast_connected
            )

            ControlTarget.CuerCast -> remoteView.setImageViewResource(
                R.id.notif_target_icon,
                R.drawable.ic_notif_cuer_cast_connected
            )

            ControlTarget.FloatingWindow -> remoteView.setImageViewResource(
                R.id.notif_target_icon,
                R.drawable.ic_notif_floating
            )
        }
        remoteView.setTextViewText(R.id.notif_target, state.targetDetails.name)

        // disconnect
        val disconnectPendingIntent: PendingIntent = pendingIntent(CastServiceContract.ACTION_DISCONNECT)
        remoteView.setOnClickPendingIntent(R.id.notif_button_disconnect, disconnectPendingIntent)

        // stop
        val stopPendingIntent: PendingIntent = pendingIntent(CastServiceContract.ACTION_STOP)
        remoteView.setOnClickPendingIntent(R.id.notif_button_stop, stopPendingIntent)
        when (state.targetDetails.target) {
            ControlTarget.CuerCast -> remoteView.setViewVisibility(R.id.notif_button_stop, VISIBLE)
            else -> remoteView.setViewVisibility(R.id.notif_button_stop, GONE)
        }

        // volume control
        remoteView.setViewVisibility(R.id.notif_group_vol, if (!showVolumeControls) GONE else VISIBLE)

        val mutePendingIntent: PendingIntent = pendingIntent(ACTION_VOL_MUTE)
        remoteView.setOnClickPendingIntent(R.id.notif_button_vol_mute, mutePendingIntent)

        val volUpPendingIntent: PendingIntent = pendingIntent(ACTION_VOL_UP)
        remoteView.setOnClickPendingIntent(R.id.notif_button_vol_up, volUpPendingIntent)

        val volDownPendingIntent: PendingIntent = pendingIntent(ACTION_VOL_DOWN)
        remoteView.setOnClickPendingIntent(R.id.notif_button_vol_down, volDownPendingIntent)

        remoteView.setTextViewText(R.id.notif_text_vol, "${(state.volumeFraction * 100f).toInt()}%")

        return builder.build()
    }

    private fun pendingIntent(action: String, extraMap: Map<String, String>? = null): PendingIntent {
        val intent = Intent(service, service::class.java).apply {
            this.action = action
            putExtra(Notification.EXTRA_NOTIFICATION_ID, FOREGROUND_ID)
            extraMap?.keys?.forEach { key ->
                putExtra(key, extraMap[key])
            }
        }

        var flags = FLAG_IMMUTABLE
        if ((extraMap?.size ?: 0) > 0) flags = flags or FLAG_UPDATE_CURRENT
        return PendingIntent.getService(service, 0, intent, flags)
    }

    companion object {
        val FOREGROUND_ID = if (BuildConfig.DEBUG) 34563 else 34564
    }
}
