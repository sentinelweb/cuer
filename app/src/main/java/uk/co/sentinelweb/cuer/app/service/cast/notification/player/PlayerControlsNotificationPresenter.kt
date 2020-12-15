package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract.Presenter
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract.PresenterExternal
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.PlayerControls.Listener
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*


class PlayerControlsNotificationPresenter constructor(
    private val view: PlayerControlsNotificationContract.View,
    private val state: PlayerControlsNotificationState,
    private val toastWrapper: ToastWrapper,
    private val log: LogWrapper,
    private val context: Context

) : PresenterExternal, Presenter {

    private val listeners: MutableList<Listener> = mutableListOf()

    override fun handleAction(action: String?) {
        when (action) {
            ACTION_PAUSE ->
                listeners.forEach { it.pause() }
            ACTION_PLAY ->
                listeners.forEach { it.play() }
            ACTION_SKIPF ->
                listeners.forEach { it.seekTo(state.positionMs + 30000) }
            ACTION_SKIPB ->
                listeners.forEach { it.seekTo(state.positionMs - 30000) }
            ACTION_TRACKB ->
                listeners.forEach { it.trackBack() }
            ACTION_TRACKF ->
                listeners.forEach { it.trackFwd() }
            else -> Unit
        }
    }

    override fun destroy() {
        state.bitmap = null
    }

    override fun setPlayerState(playState: PlayerStateDomain) {
        state.playState = playState
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
        state.bitmap?.let {
            view.showNotification(state.playState, state.media!!, it)
        } ?: state.media?.image?.let { image ->
            Glide.with(context).asBitmap().load(image.url).into(BitmapLoadTarget())
        } ?: view.showNotification(state.playState, state.media, null)
    }

    inner class BitmapLoadTarget : CustomTarget<Bitmap?>() {
        override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap?>?) {
            state.bitmap = bitmap
            view.showNotification(state.playState, state.media, bitmap)
        }

        override fun onLoadCleared(placeholder: Drawable?) {}
    }

    override fun addListener(l: Listener) {
        listeners.add(l)
    }

    override fun removeListener(l: Listener) {
        listeners.remove(l)
    }

    override fun setCurrentSecond(second: Float) {
        state.positionMs = second.toLong() * 1000
    }

    override fun setDuration(duration: Float) {
        state.durationMs = duration.toLong() * 1000
    }

    override fun error(msg: String) {
        toastWrapper.show(msg)
    }

    override fun setTitle(title: String) {
        state.title = title
    }

    override fun setConnectionState(connState: CastPlayerContract.ConnectionState) {

    }

    override fun setMedia(media: MediaDomain) {
        if (state.media?.id != media.id) {
            state.bitmap = null
        }
        state.media = media
        updateNotification()
        log.d("got media: $media")
    }

    override fun initMediaRouteButton() {

    }

    override fun reset() {
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
    }
}