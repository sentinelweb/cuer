package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import com.roche.mdas.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract.Presenter
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract.PresenterExternal
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.PlayerControls.Listener

class PlayerControlsNotificationPresenter constructor(
    private val view: PlayerControlsNotificationContract.View,
    private val state: PlayerControlsNotificationState,
    private val toastWrapper: ToastWrapper
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
        }
    }

    override fun show() {
        view.showNotification(true)
    }

    override fun setConnectionState(connState: CastPlayerContract.ConnectionState) {

    }

    override fun setPlayerState(playState: CastPlayerContract.PlayerStateUi) {
        when (playState) {
            CastPlayerContract.PlayerStateUi.UNKNOWN -> {
            }
            CastPlayerContract.PlayerStateUi.UNSTARTED -> {
            }
            CastPlayerContract.PlayerStateUi.ENDED -> {
            }
            CastPlayerContract.PlayerStateUi.PLAYING -> view.showNotification(true)
            CastPlayerContract.PlayerStateUi.PAUSED -> view.showNotification(false)
            CastPlayerContract.PlayerStateUi.BUFFERING -> {
            }
            CastPlayerContract.PlayerStateUi.VIDEO_CUED -> {
            }
        }
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
        toastWrapper.showToast(msg)
    }

    override fun setTitle(title: String) {
        state.title = title
    }

    override fun reset() {

    }

    override fun restoreState() {

    }

    override fun initMediaRouteButton() {

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