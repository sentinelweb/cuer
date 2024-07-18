package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract.Controller
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract.External
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState.Connected
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerControls.Listener
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayerControlsNotificationController constructor(
    private val view: PlayerControlsNotificationContract.View,
    private val state: PlayerControlsNotificationContract.State,
    private val toastWrapper: ToastWrapper,
    private val log: LogWrapper,
    private val context: Context,
    private val skipControl: SkipContract.External,
    private val mediaSessionManager: MediaSessionContract.Manager,
    private val res: ResourceWrapper,
) : External, Controller, SkipContract.Listener {

    private var listener: Listener? = null

    init {
        log.tag(this)
        skipControl.listener = this
    }

    override fun handleAction(action: String?) {
        when (action) {
            ACTION_PAUSE ->
                listener?.pause()
            ACTION_PLAY ->
                listener?.play()
            ACTION_SKIPF ->
                skipControl.skipFwd()
            ACTION_SKIPB ->
                skipControl.skipBack()
            ACTION_TRACKB ->
                listener?.trackBack()
            ACTION_TRACKF ->
                listener?.trackFwd()
            else -> Unit
        }
    }

    override fun destroy() {
        state.bitmap = null
    }

    override fun setIcon(icon: Int) {
        view.setIcon(icon)
    }

    override fun setBlocked(blocked: Boolean) {
        state.blocked = blocked
        view.showNotification(state)
    }

    override fun setPlayerState(playState: PlayerStateDomain) {
        state.playState = playState
        skipControl.stateChange(playState)
        when (playState) {
            UNKNOWN -> Unit
            UNSTARTED -> Unit
            ENDED -> Unit
            PLAYING -> updateNotification()
            PAUSED -> updateNotification()
            BUFFERING -> updateNotification()
            VIDEO_CUED -> Unit
            ERROR -> updateNotification()
        }
    }

    private fun updateNotification() {
        //log.d("updateNotification: state.media=${state.media?.stringMedia()}")
        listener?.apply { mediaSessionManager.checkCreateMediaSession(this) }
        state.media?.apply {
            state.bitmap
                ?.let { view.showNotification(state) }
                ?: state.media?.image?.let { image ->
                    Glide.with(context).asBitmap()
                        .load(image.url)
                        .into(BitmapLoadTarget())
                }
                ?: view.showNotification(state)
        } ?: run {
            state.bitmap = null
            view.showNotification(state)
        }
    }

    inner class BitmapLoadTarget : CustomTarget<Bitmap?>() {
        override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap?>?) {
            if (bitmap.width > 0) {
                state.bitmap = bitmap
            }
            view.showNotification(state)
        }

        override fun onLoadCleared(placeholder: Drawable?) {}
    }

    override fun addListener(l: Listener) {
        listener = l
    }

    override fun removeListener(l: Listener) {
        if (listener == l) {
            listener = null
        }
    }

    override fun setCurrentSecond(secondsFloat: Float) {
        state.positionMs = secondsFloat.toLong() * 1000
        skipControl.updatePosition(state.positionMs)
    }

    override fun setDuration(durationFloat: Float) {
        state.durationMs = durationFloat.toLong() * 1000
        skipControl.duration = state.durationMs
    }

    override fun error(msg: String) {
        toastWrapper.show(msg)
    }

    override fun setTitle(title: String) {
        state.title = title
    }

    override fun setConnectionState(connState: PlayerContract.CastConnectionState) {
        if (connState == Connected) {
            view.stopSelf()
        }
    }

    override fun setPlaylistName(name: String) {
        state.playlistName = name
    }

    override fun setPlaylistImage(image: ImageDomain?) = Unit

    override fun setPlaylistItem(playlistItem: PlaylistItemDomain?) {
        if (state.media?.id != playlistItem?.media?.id) {
            state.bitmap = null
        }
        state.media = playlistItem?.media
        updateNotification()
    }

    override fun reset() {
        //log.e("reset: state.media=${state.media?.stringMedia()}", Exception())
        state.bitmap = null
        state.media = null
        updateNotification()
    }

    override fun skipSeekTo(target: Long) {
        listener?.seekTo(target)
    }

    override fun disconnectSource() = Unit

    override fun seekTo(ms: Long) = Unit

    override fun getPlaylistItem() = null // add pli to state if needed
    override fun setButtons(buttons: PlayerContract.View.Model.Buttons) {
        state.nextEnabled = buttons.nextTrackEnabled
        state.prevEnabled = buttons.prevTrackEnabled
        state.seekEnabled = buttons.seekEnabled
        updateNotification()
    }

    override fun initMediaRouteButton() = Unit

    override fun restoreState() = Unit

    override fun skipSetBackText(text: String) = Unit

    override fun skipSetFwdText(text: String) = Unit

    companion object {
        const val ACTION_PAUSE = "pause"
        const val ACTION_PLAY = "play"
        const val ACTION_SKIPF = "skipf"
        const val ACTION_SKIPB = "skipb"
        const val ACTION_TRACKF = "trackf"
        const val ACTION_TRACKB = "trackb"
        const val ACTION_DISCONNECT = "disconnect"
        const val ACTION_STAR = "star"
    }
}