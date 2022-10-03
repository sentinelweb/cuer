package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ConnectionState
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ConnectionState.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerControls
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
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
    private val res: ResourceWrapper,
    private val playUseCase: PlayUseCase
) : CastPlayerContract.Presenter, PlayerControls, SkipContract.Listener {

    init {
        log.tag(this)
        skipControl.listener = this
    }

    private var listener: PlayerControls.Listener? = null // todo data only here move to presenter?

    override fun initialise() {
        state.isDestroyed = false
    }

    override fun onResume() {
        skipControl.updateSkipTimes()
        view.setSkipBackText(skipControl.skipBackText)
        view.setSkipFwdText(skipControl.skipForwardText)
    }

    override fun onSupport() {
        state.playlistItem?.media
            ?.also { view.showSupport(it) }
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
        listener?.apply {
            when (state.playState) {
                PLAYING -> listener?.pause()
                VIDEO_CUED, UNSTARTED, PAUSED, UNKNOWN -> listener?.play()
                else -> Unit
            }
        } ?: run {
            playUseCase.playLogic(
                state.playlistItem ?: throw IllegalStateException("No playlist item"),
                null,
                false
            )
        }
    }

    override fun onDestroyView() {
        state.isDestroyed = true
    }

    override fun addListener(l: PlayerControls.Listener) {
        listener = l
    }

    override fun removeListener(l: PlayerControls.Listener) {
        if (listener == l) {
            listener = null
        }
    }

    override fun onSeekBackPressed() {
        skipControl.skipBack()
    }

    override fun onSeekFwdPressed() {
        skipControl.skipFwd()
    }

    override fun onTrackBackPressed() {
        listener?.trackBack()
    }

    override fun onTrackFwdPressed() {
        listener?.trackFwd()
    }

    override fun onSeekChanged(ratio: Float) {
        if (state.durationMs > 0) {
            state.seekPositionMs = (ratio * state.durationMs).toLong()
            view.setPosition(mapper.formatTime(state.seekPositionMs))
        }
    }

    override fun onSeekFinished() {
        state.playState = BUFFERING
        listener?.seekTo(state.seekPositionMs)
        state.seekPositionMs = 0
    }

    override fun initMediaRouteButton() {
        view.initMediaRouteButton()
    }

    override fun setConnectionState(connState: ConnectionState) {
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
        view.setState(playState)
    }

    override fun setCurrentSecond(second: Float) {
        state.positionMs = (second * 1000).toLong()
        skipControl.updatePosition(state.positionMs)
        if (state.durationMs > 0) {
            if (!state.isLiveStream) {
                view.setPosition(mapper.formatTime(state.positionMs))
                if (state.playState != BUFFERING) {
                    view.updateSeekPosition(state.positionMs / state.durationMs.toFloat())
                }
                view.setLiveTime(null)
            } else {
                listener
                    ?.getLiveOffsetMs()
                    ?.apply { view.setLiveTime(mapper.formatLiveTime(this)) }
                    ?.takeIf { it > 9 * 1000 }
                    ?.apply { view.setPosition("-" + mapper.formatTime(this)) }
                    ?: run { view.setPosition("") }
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
            state.positionMs = 0L.apply { view.setPosition("") }
            state.durationMs = 0L.apply { view.setDuration("") }
            view.clearImage()
            view.setPaused()
            view.setDurationColors(R.color.text_primary, R.color.transparent)
            view.setSeekEnabled(false)
            view.updateSeekPosition(0f)
            view.setLiveTime(null)
        }
    }

    override fun restoreState() {
        view.setTitle(state.title)
    }

    override fun disconnectSource() {
        view.setPaused()
        view.setSeekEnabled(false)
        view.setLiveTime(null)
    }

    override fun setPlaylistName(name: String) {
        state.playlistName = name
        view.setPlaylistName(name)
    }

    override fun setPlaylistItem(
        playlistItem: PlaylistItemDomain?,
        source: OrchestratorContract.Source
    ) {
        state.source = source
        state.playlistItem = playlistItem?.apply {
            media.thumbNail?.url?.apply { view.setImage(this) }
            media.title?.apply { state.title = this }
            media.title?.let { view.setTitle(it) }
            state.durationMs = media.duration ?: 0L
            state.isLiveStream = media.isLiveBroadcast
            state.isUpcoming = media.isLiveBroadcastUpcoming
            if (state.isLiveStream) {
                view.setPosition("-")
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

    override fun onPlaylistClick() {// todo get source
        state.playlistItem?.playlistId?.let {
            view.navigate(
                NavigationModel(
                    PLAYLIST,
                    mapOf(PLAYLIST_ID to it, PLAY_NOW to false, SOURCE to state.source)
                )
            )
        }
    }

    override fun onPlaylistItemClick() {// todo get source
        state.playlistItem?.let {
            view.navigate(
                NavigationModel(
                    PLAYLIST_ITEM,
                    mapOf(
                        NavigationModel.Param.PLAYLIST_ITEM to it,
                        FRAGMENT_NAV_EXTRAS to view.makeItemTransitionExtras(),
                        SOURCE to state.source
                    )
                )
            )
        }
    }

    override fun seekTo(ms: Long) {
        listener?.seekTo(ms)
    }

    override fun getPlaylistItem(): PlaylistItemDomain? = state.playlistItem

    override fun skipSeekTo(target: Long) {
        listener?.seekTo(target)
    }



    override fun skipSetBackText(text: String) {
        view.setSkipBackText(text)
    }

    override fun skipSetFwdText(text: String) {
        view.setSkipFwdText(text)
    }

}