package uk.co.sentinelweb.cuer.app.util.cast.ui

import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState.*
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.PlayerStateUi.*
import kotlin.math.max
import kotlin.math.min

class CastPlayerPresenter(
    private val view: CastPlayerContract.View,
    private val state: CastPlayerState
) : CastPlayerContract.Presenter, CastPlayerContract.PresenterExternal {

    override fun addListener(l: CastPlayerContract.PresenterExternal.Listener) {
        state.listeners.add(l)
    }

    override fun removeListener(l: CastPlayerContract.PresenterExternal.Listener) {
        state.listeners.remove(l)
    }

    override fun onPlayPressed() {
        state.listeners.forEach { it.playPressed() }
    }

    override fun onPausePressed() {
        state.listeners.forEach { it.pausePressed() }
    }

    override fun onSeekBackPressed() {
        if (state.durationMs > 0) {
            state.listeners.forEach { it.onSeekChanged(max(0, state.positionMs - 30000)) }
        }
    }

    override fun onSeekFwdPressed() {
        if (state.durationMs > 0) {
            state.listeners.forEach {
                it.onSeekChanged(min(state.durationMs, state.positionMs + 30000))
            }
        }
    }

    override fun onTrackBackPressed() {
        state.listeners.forEach { it.trackBackPressed() }
    }

    override fun onTrackFwdPressed() {
        state.listeners.forEach { it.trackFwdPressed() }
    }

    override fun onSeekChanged(ratio: Float) {
        if (state.durationMs > 0) {
            state.seekPositionMs = (ratio * state.durationMs).toLong()
            view.setCurrentSecond("${state.seekPositionMs / 1000} s") // todo map time
        }
    }

    override fun onSeekFinished() {
        state.listeners.forEach { it.onSeekChanged(state.seekPositionMs) }
        state.seekPositionMs = 0
    }

    override fun initMediaRouteButton() {
        view.initMediaRouteButton()
    }

    override fun setConnectionState(connState: CastPlayerContract.ConnectionState) {
        state.connectionState = connState
        view.setConnectionText(
            when (connState) {
                CC_DISCONNECTED -> "X"
                CC_CONNECTING -> "*"
                CC_CONNECTED -> "="
            }
        )
    }

    override fun setPlayerState(playState: CastPlayerContract.PlayerStateUi) {
        state.playState = playState
        when (playState) {
            UNKNOWN -> view.setPaused() // todo better state
            UNSTARTED -> view.setPaused() // todo better state
            ENDED -> view.setPaused()
            PLAYING -> view.setPlaying()
            PAUSED -> view.setPaused()
            BUFFERING -> view.setBuffering()
            VIDEO_CUED -> TODO()
        }
    }

    override fun setCurrentSecond(second: Float) {
        state.positionMs = (second * 1000).toLong()
        view.setCurrentSecond("${state.positionMs / 1000} s") // todo map time
        if (state.durationMs > 0) {
            view.updateSeekPosition(state.positionMs / state.durationMs.toFloat())
        }
    }

    override fun setDuration(duration: Float) {
        state.durationMs = (duration * 1000).toLong()
        view.setDuration("${state.durationMs / 1000} s") // todo map time
    }

    override fun error(msg: String) {
        view.showMessage(msg)
    }

    override fun setTitle(title: String) {
        state.title = title
        view.setTitle(title)
    }

    override fun reset() {
        state.title = "".apply { view.setTitle(this) }
        state.positionMs = 0L.apply { view.setCurrentSecond(this.toString()) }
        state.durationMs = 0L.apply { view.setDuration(this.toString()) }
        view.setPaused()
    }

    override fun restoreState() {
        view.setTitle(state.title)
        // todo restore state
    }
}