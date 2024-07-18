package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.DurationStyle.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.usecase.CastUseCase
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.mappers.PlaylistAndItemMapper

class CastPlayerPresenter(
    private val view: CastPlayerContract.View,
    private val state: CastPlayerContract.State,
    private val mapper: CastPlayerUiMapper,
    private val log: LogWrapper,
    private val skipControl: SkipContract.External,
    private val playUseCase: PlayUseCase,
    private val playlistAndItemMapper: PlaylistAndItemMapper,
    private val castUseCase: CastUseCase,
) : CastPlayerContract.Presenter, PlayerContract.PlayerControls, SkipContract.Listener {

    init {
        log.tag(this)
        skipControl.listener = this
    }

    private var listener: PlayerContract.PlayerControls.Listener? = null

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
                PlayerStateDomain.PLAYING -> listener?.pause()
                PlayerStateDomain.VIDEO_CUED, PlayerStateDomain.UNSTARTED, PlayerStateDomain.PAUSED, PlayerStateDomain.UNKNOWN -> listener?.play()
                else -> Unit
            }
        } ?: run {
            playUseCase.playLogic(
                playlistAndItemMapper.map(state.playlistItem ?: throw IllegalStateException("No playlist item")),
                false
            )
        }
    }

    override fun onDestroyView() {
        state.isDestroyed = true
    }

    override fun addListener(l: PlayerContract.PlayerControls.Listener) {
        listener = l
    }

    override fun removeListener(l: PlayerContract.PlayerControls.Listener) {
        if (listener == l) {
            listener = null
        }
    }

    override fun setCastDetails(details: CastPlayerContract.State.CastDetails) {
        state.castDetails = details
        view.setCastDetails(details)
    }

    override fun onCastClick() {
        castUseCase.showCastDialog()
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
        state.playState = PlayerStateDomain.BUFFERING
        listener?.seekTo(state.seekPositionMs)
        state.seekPositionMs = 0
    }

//    override fun initMediaRouteButton() {
//        view.initMediaRouteButton()
//    }

    override fun setPlayerState(playState: PlayerStateDomain) {
        state.playState = playState
        log.d("playState = $playState")
        skipControl.stateChange(playState)
        when (playState) {
            PlayerStateDomain.PLAYING -> view.setPlaying()
            PlayerStateDomain.ENDED -> view.setPaused()
            PlayerStateDomain.PAUSED -> view.setPaused()
            PlayerStateDomain.UNKNOWN,
            PlayerStateDomain.UNSTARTED,
            PlayerStateDomain.BUFFERING -> view.showBuffering()

            PlayerStateDomain.VIDEO_CUED -> Unit
            PlayerStateDomain.ERROR -> {
                view.setPaused(); view.showMessage("An error occurred")
            }
        }
        view.setState(playState)
    }

    override fun setCurrentSecond(secondsFloat: Float) {
        state.positionMs = (secondsFloat * 1000).toLong()
        skipControl.updatePosition(state.positionMs)
        if (state.durationMs > 0) {
            if (!state.isLiveStream) {
                view.setPosition(mapper.formatTime(state.positionMs))
                if (state.playState != PlayerStateDomain.BUFFERING) {
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

    override fun setDuration(durationFloat: Float) {
        state.durationMs = (durationFloat * 1000).toLong()
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
            state.positionMs = 0L
            view.setPosition("-:-")
            state.durationMs = 0L
            view.setDuration("-:-")
            view.clearImage()
            view.setPaused()
            view.setDurationStyle(Normal)
            view.updateSeekPosition(0f)
            view.setLiveTime(null)
            updateButtons()
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

    override fun setPlaylistItem(playlistItem: PlaylistItemDomain?) {
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
                    view.setDurationStyle(Upcoming)
                } else {
                    view.setDurationStyle(Live)
                }
                view.updateSeekPosition(1f)

            } else {
                view.setDuration(mapper.formatTime(state.durationMs))
                view.setDurationStyle(Normal)
            }
//            view.setSeekEnabled(!state.isLiveStream)
        }
    }

    override fun setPlaylistImage(image: ImageDomain?) {
        view.setPlaylistImage(image?.url)
    }

    override fun onPlaylistClick() {
        state.playlistItem?.playlistId?.let {
            view.navigate(
                NavigationModel(
                    NavigationModel.Target.PLAYLIST,
                    mapOf(
                        NavigationModel.Param.PLAYLIST_ID to it.id.value,
                        NavigationModel.Param.PLAY_NOW to false,
                        NavigationModel.Param.SOURCE to it.source
                    )
                )
            )
        }
    }

    override fun onPlaylistItemClick() {
        state.playlistItem?.let {
            view.navigate(
                NavigationModel(
                    NavigationModel.Target.PLAYLIST_ITEM,
                    mapOf(
                        NavigationModel.Param.PLAYLIST_ITEM to it,
                        //FRAGMENT_NAV_EXTRAS to view.makeItemTransitionExtras(),
                        NavigationModel.Param.SOURCE to it.id!!.source
                    )
                )
            )
        }
    }

    override fun seekTo(ms: Long) {
        listener?.seekTo(ms)
    }

    override fun getPlaylistItem(): PlaylistItemDomain? = state.playlistItem
    override fun setButtons(buttons: PlayerContract.View.Model.Buttons) {
        state.buttons = buttons
        updateButtons()
    }

    private fun updateButtons() {
        view.setSeekEnabled(state.buttons?.seekEnabled ?: false)
        view.setNextTrackEnabled(state.buttons?.nextTrackEnabled ?: false)
        view.setPrevTrackEnabled(state.buttons?.prevTrackEnabled ?: false)
    }

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