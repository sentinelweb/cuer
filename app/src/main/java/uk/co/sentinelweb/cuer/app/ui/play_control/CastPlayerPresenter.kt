package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_ITEM_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.ConnectionState.*
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class CastPlayerPresenter(
    private val view: CastPlayerContract.View,
    private val state: CastPlayerContract.State,
    private val mapper: CastPlayerUiMapper,
    private val log: LogWrapper,
    private val skipControl: SkipContract.External,
    private val res: ResourceWrapper
) : CastPlayerContract.Presenter, CastPlayerContract.PlayerControls, SkipContract.Listener {

    init {
        log.tag(this)
        skipControl.listener = this
    }

    override fun initialise() {
        state.isDestroyed = false
        view.setSkipBackText(skipControl.skipBackText)
        view.setSkipFwdText(skipControl.skipForwardText)
    }

    override fun onSeekBackSelectTimePressed(): Boolean {
        skipControl.onSelectSkipTime(false)
        return true
    }

    override fun onSeekSelectTimeFwdPressed(): Boolean {
        skipControl.onSelectSkipTime(true)
        return true
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
        skipControl.skipBack()
    }

    override fun onSeekFwdPressed() {
        skipControl.skipFwd()
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
        skipControl.stateChange(playState)
        when (playState) {
            PLAYING -> view.setPlaying()
            ENDED -> view.setPaused()
            PAUSED -> view.setPaused()
            UNKNOWN,
            UNSTARTED,
            BUFFERING -> view.showBuffering()
            VIDEO_CUED -> Unit
            ERROR -> {
                view.setPaused(); view.showMessage("An error occurred")
            }
        }
    }

    override fun setCurrentSecond(second: Float) {
        state.positionMs = (second * 1000).toLong()
        skipControl.updatePosition(state.positionMs)
        if (state.durationMs > 0) {
            if (!state.isLiveStream) {
                view.setCurrentSecond(mapper.formatTime(state.positionMs))
                view.updateSeekPosition(state.positionMs / state.durationMs.toFloat())
            }
        }
    }

    override fun setDuration(duration: Float) {
        state.durationMs = (duration * 1000).toLong()
        skipControl.duration = state.durationMs
        if (!state.isLiveStream) {
            view.setDuration(mapper.formatTime(state.durationMs))
        }
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
            view.setDurationColors(R.color.text_primary, R.color.transparent)
            view.setSeekEnabled(false)
            view.updateSeekPosition(0f)
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
            state.isLiveStream = media.isLiveBroadcast
            state.isUpcoming = media.isLiveBroadcastUpcoming
            if (state.isLiveStream) {
                view.setCurrentSecond("-")
                if (state.isUpcoming) {
                    view.setDuration(res.getString(R.string.upcoming))
                    view.setDurationColors(R.color.white, R.color.upcoming_background)
                } else {
                    view.setDuration(res.getString(R.string.live))
                    view.setDurationColors(R.color.white, R.color.live_background)
                }
                view.updateSeekPosition(1f)

            } else {
                view.setDuration(mapper.formatTime(state.durationMs))
                view.setDurationColors(R.color.text_primary, R.color.transparent)
            }
            view.setSeekEnabled(!state.isLiveStream)
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

    override fun skipSeekTo(target: Long) {
        state.listeners.forEach {
            it.seekTo(target)
        }
    }

    override fun skipSetBackText(text: String) {
        view.setSkipBackText(text)
    }

    override fun skipSetFwdText(text: String) {
        view.setSkipFwdText(text)
    }

}