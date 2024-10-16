package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.FOLDER_LIST
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.DurationStyle.*
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.State.TargetDetails
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ControlTarget.Local
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain.FILESYSTEM
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.name
import uk.co.sentinelweb.cuer.domain.mappers.PlaylistAndItemMapper
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository

class CastPlayerPresenter(
    private val view: CastPlayerContract.View,
    private val state: CastPlayerContract.State,
    private val mapper: CastPlayerUiMapper,
    private val log: LogWrapper,
    private val skipControl: SkipContract.External,
    private val playUseCase: PlayUseCase,
    private val playlistAndItemMapper: PlaylistAndItemMapper,
    private val remotesRepository: RemotesRepository,
) : CastPlayerContract.Presenter, PlayerContract.PlayerControls, SkipContract.Listener {

    private lateinit var castController: CastController

    init {
        log.tag(this)
        skipControl.listener = this
    }

    override fun setCastController(castController: CastController) {
        this.castController = castController
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

    override fun setCastDetails(details: TargetDetails) {
        state.targetDetails = details
        view.setTargetDetails(details)
    }

    override fun onCastClick() {
        castController.showCastDialog()
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
        skipControl.stateChange(playState)
        when (playState) {
            PlayerStateDomain.PLAYING -> view.setPlaying()
            PlayerStateDomain.ENDED -> view.setPaused()
            PlayerStateDomain.PAUSED -> view.setPaused()
            PlayerStateDomain.UNKNOWN -> view.setPaused()
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
            view.setTargetDetails(TargetDetails(Local))
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
        state.playlistItem = playlistItem
        playlistItem
            ?.apply {
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
            }
            ?: run {
                view.setPosition("--:--")
                view.setDuration("--:--")
                view.setDurationStyle(Normal)
                view.setTitle("No media")
                state.durationMs = 0L
                state.isLiveStream = false
                state.isUpcoming = false
                view.clearImage()
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
                        Param.PLAYLIST_ID to it.id.value,
                        Param.PLAY_NOW to false,
                        SOURCE to it.source
                    )
                )
            )
        }
    }

    override fun onPlaylistItemClick() {
        state.playlistItem?.let { item ->
            when (item.media.platform) {
                FILESYSTEM -> (item.id?.locator
                    ?.also { log.d("locator: $it") }
                    ?.let { remotesRepository.getByLocator(it) })
                    ?.also { log.d("found: ${it.name()}") }
                    ?.let { foundNode ->
                        NavigationModel(
                            FOLDER_LIST,
                            mapOf(
                                REMOTE_ID to foundNode.id?.id?.value,
                                FILE_PATH to item.media.platformId.substringBeforeLast("/")
                            )
                        )
                    }

                else -> NavigationModel(
                    PLAYLIST_ITEM,
                    mapOf(
                        Param.PLAYLIST_ITEM to item,
                        //FRAGMENT_NAV_EXTRAS to view.makeItemTransitemionExtras(),
                        SOURCE to item.id!!.source
                    )
                )
            }?.also { view.navigate(it) }

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

    override fun setVolume(fraction: Float) {
        state.volumeFraction = fraction
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
