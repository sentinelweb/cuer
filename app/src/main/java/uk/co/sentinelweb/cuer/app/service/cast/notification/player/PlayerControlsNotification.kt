package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract.External
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract.Presenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.ConnectionState.CC_DISCONNECTED
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.PlayerControls.Listener
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayerControlsNotification constructor(
    private val view: PlayerControlsNotificationContract.View,
    private val state: PlayerControlsNotificationState,
    private val toastWrapper: ToastWrapper,
    private val log: LogWrapper,
    private val context: Context,
    private val skipControl: SkipContract.External,
    private val mediaSessionManager: MediaSessionManager
) : External, Presenter, SkipContract.Listener {

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
            state.bitmap?.let { view.showNotification(state.playState, state.media, it) }
                ?: state.media?.image?.let { image ->
                    Glide.with(context).asBitmap().load(image.url).into(BitmapLoadTarget())
                } ?: view.showNotification(state.playState, state.media, null)
        } ?: run {
            state.bitmap = null
            view.showNotification(state.playState, null, null)
        }
    }

    inner class BitmapLoadTarget : CustomTarget<Bitmap?>() {
        override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap?>?) {
            state.bitmap = bitmap
            view.showNotification(state.playState, state.media, bitmap)
        }

        override fun onLoadCleared(placeholder: Drawable?) {}
    }

    override fun addListener(l: Listener) {
        listener = l
    }

    override fun removeListener(l: Listener) {
        if (listener == l) {
            listener = null;
        }
    }

    override fun setCurrentSecond(second: Float) {
        state.positionMs = second.toLong() * 1000
        skipControl.updatePosition(state.positionMs)
    }

    override fun setDuration(duration: Float) {
        state.durationMs = duration.toLong() * 1000
        skipControl.duration = state.durationMs
    }

    override fun error(msg: String) {
        toastWrapper.show(msg)
    }

    override fun setTitle(title: String) {
        state.title = title
    }

    override fun setConnectionState(connState: CastPlayerContract.ConnectionState) {
        if (connState == CC_DISCONNECTED) {
            view.stopSelf()
        }
    }

    override fun setPlaylistName(name: String) {
        state.playlistName = name
    }

    override fun setPlaylistImage(image: ImageDomain?) {
        // not needed here
    }

    override fun setPlaylistItem(playlistItem: PlaylistItemDomain?, source: OrchestratorContract.Source) {
        if (state.media?.id != playlistItem?.media?.id) {
            state.bitmap = null
        }
        state.media = playlistItem?.media
        updateNotification()
    }

    override fun initMediaRouteButton() {
        // not needed here
    }

    override fun reset() {
        //log.e("reset: state.media=${state.media?.stringMedia()}", Exception())
        state.bitmap = null
        state.media = null
        updateNotification()
    }

    override fun restoreState() {

    }

    companion object {
        const val ACTION_PAUSE = "pause"
        const val ACTION_PLAY = "play"
        const val ACTION_SKIPF = "skipf"
        const val ACTION_SKIPB = "skipb"
        const val ACTION_TRACKF = "trackf"
        const val ACTION_TRACKB = "trackb"
        const val ACTION_DISCONNECT = "disconnect"
        const val ACTION_STAR = "star"
        const val ACTION_UNSTAR = "unstar"

    }

    override fun skipSeekTo(target: Long) {
        listener?.seekTo(target)
    }

    override fun skipSetBackText(text: String) {

    }

    override fun skipSetFwdText(text: String) {

    }
}