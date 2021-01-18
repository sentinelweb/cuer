package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_ITEM_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.ConnectionState.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import kotlin.math.max
import kotlin.math.min

class CastPlayerPresenter(
    private val view: CastPlayerContract.View,
    private val state: CastPlayerContract.State,
    private val mapper: CastPlayerUiMapper,
    private val log: LogWrapper
) : CastPlayerContract.Presenter, CastPlayerContract.PlayerControls {

    init {
        log.tag(this)
    }

    override fun initialise() {
        state.isDestroyed = false
    }

    override fun onPlayPausePressed() {
        when (state.playState) {
            PLAYING -> state.listeners.forEach { it.pause() }
            VIDEO_CUED, UNSTARTED, PAUSED, UNKNOWN -> state.listeners.forEach { it.play() }
            else -> Unit
        }
    }

    override fun onDestroyView() {
        state.isDestroyed = true
    }

    override fun addListener(l: CastPlayerContract.PlayerControls.Listener) {
        if (!state.listeners.contains(l)) {
            state.listeners.add(l)
        }
    }

    override fun removeListener(l: CastPlayerContract.PlayerControls.Listener) {
        state.listeners.remove(l)
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
        when (connState) {
            CC_DISCONNECTED -> view.hideBuffering()
            CC_CONNECTING -> view.showBuffering()
            CC_CONNECTED -> view.hideBuffering()
        }
    }

    override fun setPlayerState(playState: PlayerStateDomain) {
        state.playState = playState
        log.d("playState = $playState")
        when (playState) {
            UNKNOWN -> view.showBuffering()
            UNSTARTED -> view.showBuffering()
            ENDED -> view.setPaused()
            PLAYING -> view.setPlaying()
            PAUSED -> view.setPaused()
            BUFFERING -> view.showBuffering()
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

    override fun setPlaylistName(name: String) {
        view.setPlaylistName(name)
    }

    override fun setPlaylistItem(playlistItem: PlaylistItemDomain?) {
        state.playlistItem = playlistItem?.apply {
            media.thumbNail?.url?.apply { view.setImage(this) }
            media.title?.apply { state.title = this }
            media.title?.let { view.setTitle(it) }
            state.durationMs = media.duration ?: 0L
            view.setDuration(mapper.formatTime(state.durationMs))
        }
    }

    override fun setPlaylistImage(image: ImageDomain?) {
        view.setPlaylistImage(image?.url)
    }

    override fun onPlaylistClick() {
        state.playlistItem?.playlistId?.let {
            view.navigate(NavigationModel(PLAYLIST_FRAGMENT, mapOf(PLAYLIST_ID to it, PLAY_NOW to false)))
        }
    }

    override fun onPlaylistItemClick() {
        state.playlistItem?.let {
            view.navigate(
                NavigationModel(
                    PLAYLIST_ITEM_FRAGMENT,
                    mapOf(
                        PLAYLIST_ITEM to it,
                        FRAGMENT_NAV_EXTRAS to view.makeItemTransitionExtras()
                    )
                )
            )
        }
    }
}