package uk.co.sentinelweb.cuer.app.util.cast.ui

import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState.*
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import kotlin.math.max
import kotlin.math.min

class CastPlayerPresenter(
    private val view: CastPlayerContract.View,
    private val state: CastPlayerState,
    private val mapper: CastPlayerUiMapper
) : CastPlayerContract.Presenter, CastPlayerContract.PlayerControls {

    override fun initialise() {
        state.isDestroyed = false
    }

    override fun onDestroyView() {
        state.isDestroyed = true
    }

    override fun addListener(l: CastPlayerContract.PlayerControls.Listener) {
        state.listeners.add(l)
    }

    override fun removeListener(l: CastPlayerContract.PlayerControls.Listener) {
        state.listeners.remove(l)
    }

    override fun onPlayPressed() {
        state.listeners.forEach { it.play() }
    }

    override fun onPausePressed() {
        state.listeners.forEach { it.pause() }
    }

    override fun onSeekBackPressed() {
        if (state.durationMs > 0) {
            state.listeners.forEach { it.seekTo(max(0, state.positionMs - 30000)) }
        }
    }

    override fun onSeekFwdPressed() {
        if (state.durationMs > 0) {
            state.listeners.forEach {
                it.seekTo(min(state.durationMs, state.positionMs + 30000))
            }
        }
    }

    override fun onTrackBackPressed() {
        state.listeners.forEach { it.trackBack() }
    }

    override fun onTrackFwdPressed() {
        state.listeners.forEach { it.trackFwd() }
    }

    override fun onSeekChanged(ratio: Float) {
        if (state.durationMs > 0) {
            state.seekPositionMs = (ratio * state.durationMs).toLong()
            view.setCurrentSecond("${state.seekPositionMs / 1000} s") // todo map time
        }
    }

    override fun onSeekFinished() {
        state.listeners.forEach { it.seekTo(state.seekPositionMs) }
        state.seekPositionMs = 0
    }

    override fun initMediaRouteButton() {
        view.initMediaRouteButton()
    }

    override fun setConnectionState(connState: CastPlayerContract.ConnectionState) {
        view.setConnectionText(
            when (connState) {
                CC_DISCONNECTED -> "X"
                CC_CONNECTING -> "*"
                CC_CONNECTED -> "="
            }
        )
    }

    override fun setPlayerState(playState: PlayerStateDomain) {
        when (playState) {
            UNKNOWN -> view.setPaused() // todo better state
            UNSTARTED -> view.setPaused() // todo better state
            ENDED -> view.setPaused()
            PLAYING -> view.setPlaying()
            PAUSED -> view.setPaused()
            BUFFERING -> view.setBuffering()
            VIDEO_CUED -> Unit
            ERROR -> {
                view.setPaused(); view.showMessage("An error occurred")
            }
        }
    }

    override fun setCurrentSecond(second: Float) {
        state.positionMs = (second * 1000).toLong()
        view.setCurrentSecond(mapper.formatTime(state.positionMs))
        if (state.durationMs > 0) {
            view.updateSeekPosition(state.positionMs / state.durationMs.toFloat())
        }
    }

    override fun setDuration(duration: Float) {
        state.durationMs = (duration * 1000).toLong()
        view.setDuration(mapper.formatTime(state.durationMs))
    }

    override fun error(msg: String) {
        view.showMessage(msg)
    }

    override fun setTitle(title: String) {
        state.title = title
        view.setTitle(title)
    }

    override fun reset() {
        if (!state.isDestroyed) {
            state.title = "No media".apply { view.setTitle(this) }
            state.positionMs = 0L.apply { view.setCurrentSecond("") }
            state.durationMs = 0L.apply { view.setDuration("") }
            view.clearImage()
            view.setPaused()
        }
    }

    override fun restoreState() {
        view.setTitle(state.title)
        // todo restore state
    }

    override fun setMedia(media: MediaDomain) {
        media.thumbNail?.url?.apply { view.setImage(this) }
    }
}