package uk.co.sentinelweb.cuer.app.util.chromecast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackContract
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.isLiveOrUpcoming
import uk.co.sentinelweb.cuer.domain.ext.startPosition
import uk.co.sentinelweb.cuer.domain.ext.stringMedia
import kotlin.math.max
import kotlin.math.min

// todo ditch this and connect to PlayerController
class YouTubePlayerListener(
    private val state: State,
    private val queue: QueueMediatorContract.Consumer,
    private val mediaSessionManager: MediaSessionContract.Manager,
    private val log: LogWrapper,
    private val timeProvider: TimeProvider,
    private val coroutines: CoroutineContextProvider,
    private val livePlaybackController: LivePlaybackContract.Controller,
) : AbstractYouTubePlayerListener(),
    PlayerContract.PlayerControls.Listener {

    data class State constructor(
        var playState: PlayerStateDomain = PlayerStateDomain.UNKNOWN,
        var positionSec: Float = 0f,
        var durationSec: Float = 0f,
        var currentMedia: MediaDomain? = null,
        var lastUpdateMedia: Long = -1L,
        var lastUpdateUI: Long = -1L,
        var receivedVideoId: String? = null,
    )

    private var youTubePlayer: YouTubePlayer? = null

    var playerUi: PlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            value?.let {
                setupPlayer(it)
            } ?: cleanupPlayer(field)
            field = value
        }

    init {
        log.tag(this)
        queue.currentItemFlow
            .distinctUntilChanged { old, new -> old?.media?.id == new?.media?.id }
            .onEach { loadVideo(it) }
            .onEach { item -> item?.let { mediaSessionManager.setMedia(item.media, queue.playlist) } }
            .launchIn(coroutines.mainScope)
    }

    fun onDisconnected() {
        youTubePlayer?.removeListener(this)
        youTubePlayer = null
        queue.currentItem?.apply {
            playerUi?.setPlaylistItem(this)
            playerUi?.setConnectionState(PlayerContract.ConnectionState.CC_DISCONNECTED)
            playerUi?.setPlayerState(PlayerStateDomain.PAUSED)
            playerUi?.setCurrentSecond(0f)
            playerUi?.setButtons(buildButtons(this))
        } ?: run {
            playerUi?.reset()
            playerUi?.setButtons(buildButtons(null))
            playerUi = null
        }
        playerUi?.removeListener(this)
        coroutines.cancel()
    }

    // region AbstractYouTubePlayerListener
    override fun onReady(youTubePlayer: YouTubePlayer) {
        this.youTubePlayer = youTubePlayer
        log.d("ready")
        loadVideo(queue.currentItem)
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {
        this.youTubePlayer = youTubePlayer
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        // log.d("onVideoDuration dur=${state.durationSec} durObTime=${state.durationObtainedTime}")
        this.youTubePlayer = youTubePlayer
        state.durationSec = duration
        state.currentMedia
            ?.takeIf { it.isLiveBroadcast }
            ?.apply { livePlaybackController.gotDuration((duration * 1000).toLong()) }
        playerUi?.setDuration(duration)
        updateMedia(false, durSec = duration)
        playerUi?.setButtons(buildButtons(queue.currentItem))
        state.currentMedia
            ?.apply { mediaSessionManager.setMedia(this, queue.playlist) }
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        this.youTubePlayer = youTubePlayer
        if (state.positionSec != second) {
            state.positionSec = second
            updateMedia(true, posSec = second)
            livePlaybackController.setCurrentPosition((second * 1000).toLong())
        }
        if (shouldUpdateUi()) {
            playerUi?.setCurrentSecond(second)
            setTimeUpdateUi()
            updateMediaSessionManagerPlaybackState()
        }
    }

    private fun updateMediaSessionManagerPlaybackState() {
        state.currentMedia?.apply {
            mediaSessionManager.updatePlaybackState(
                this,
                state.playState,
                if (isLiveBroadcast) getLiveOffsetMs() else null,
                queue.playlist
            )
        }
    }

    private fun shouldUpdateUi() = timeProvider.currentTimeMillis() - state.lastUpdateUI > UI_UPDATE_INTERVAL

    private fun setTimeUpdateUi() {
        state.lastUpdateUI = timeProvider.currentTimeMillis()
    }

    private fun updateMedia(throttle: Boolean, posSec: Float? = null, durSec: Float? = null) {
        state.currentMedia = state.currentMedia?.run {
            copy(
                positon = posSec?.let { (it * 1000).toLong() } ?: positon,
                duration = durSec?.let { (it * 1000).toLong() } ?: duration,
                dateLastPlayed = timeProvider.instant()
            )
        }
        if (shouldUpdateMedia(throttle)) {
            if (state.receivedVideoId != null && state.receivedVideoId == state.currentMedia?.platformId) {
                state.currentMedia?.apply { queue.updateCurrentMediaItem(this) }
            } else log.d("Not updating media: ${state.receivedVideoId} != ${state.currentMedia?.platformId}")
            setTimeUpdateMedia()
        }
    }

    private fun shouldUpdateMedia(throttle: Boolean) =
        !throttle || timeProvider.currentTimeMillis() - state.lastUpdateMedia > DB_UPDATE_INTERVAL

    private fun setTimeUpdateMedia() {
        state.lastUpdateMedia = timeProvider.currentTimeMillis()
    }


    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerError) {
        this.youTubePlayer = youTubePlayer
        playerUi?.error(error.toString())
        trackFwd()
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, q: PlaybackQuality) {
        this.youTubePlayer = youTubePlayer
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, r: PlaybackRate) {
        this.youTubePlayer = youTubePlayer
    }

    override fun onStateChange(
        youTubePlayer: YouTubePlayer,
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        playerState: PlayerState,
    ) {
        this.youTubePlayer = youTubePlayer
        state.playState = when (playerState) {
            PlayerState.ENDED -> PlayerStateDomain.ENDED
            PlayerState.PAUSED -> PlayerStateDomain.PAUSED
            PlayerState.PLAYING -> PlayerStateDomain.PLAYING
            PlayerState.BUFFERING -> PlayerStateDomain.BUFFERING
            PlayerState.UNSTARTED -> PlayerStateDomain.UNSTARTED
            PlayerState.UNKNOWN -> PlayerStateDomain.UNKNOWN
            PlayerState.VIDEO_CUED -> PlayerStateDomain.VIDEO_CUED
        }
        playerUi?.setPlayerState(state.playState)
        updateMediaSessionManagerPlaybackState()
        if (state.playState == PlayerStateDomain.ENDED) {
            queue.onTrackEnded()
        }
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
        this.youTubePlayer = youTubePlayer
        state.receivedVideoId = videoId
        livePlaybackController.gotVideoId(videoId)
        log.d("Got id: $videoId media=${state.currentMedia?.stringMedia()}")
    }

    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
        this.youTubePlayer = youTubePlayer
    }
    // endregion

    // region  CastPlayerContract.PresenterExternal.Listener
    override fun play() {
        try {
            youTubePlayer?.play()
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override fun pause() {
        try {
            youTubePlayer?.pause()
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override fun trackBack() {
        try {
            queue.previousItem()
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override fun trackFwd() {
        try {
            queue.nextItem()
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override fun seekTo(positionMs: Long) {
        try {
            youTubePlayer?.seekTo(positionMs / 1000f)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override fun getLiveOffsetMs(): Long {
        return livePlaybackController.getLiveOffsetMs()
    }

    override fun skipBack() {
        try {// todo hack : should use skip control - in the wrong spot :/
            youTubePlayer?.seekTo(max(state.positionSec - 30, 0f))
        } catch (e: Exception) {
            handleError(e)
        }
    }

    override fun skipFwd() {
        try {// todo hack : should use skip control - in the wrong spot :/
            youTubePlayer?.seekTo(min(state.positionSec + 30, state.durationSec))
        } catch (e: Exception) {
            handleError(e)
        }
    }
    // endregion

    private fun handleError(e: Exception) {
        playerUi?.error("Error: ${e.message ?: "Unknown - check log"}")
        log.e("Error playing", e)
    }

    private fun loadVideo(item: PlaylistItemDomain?) {
        item?.apply {
            val startPos = media.startPosition()
            livePlaybackController.clear(media.platformId)
            log.d("loadVideo: play position: pos =  $startPos ms")
            youTubePlayer?.loadVideo(media.platformId, (startPos / 1000).toFloat())
            state.currentMedia = media
            playerUi?.setPlaylistItem(queue.currentItem)
            playerUi?.setPlaylistName(queue.playlist?.title ?: "none")
            playerUi?.setPlaylistImage(queue.playlist?.let { it.thumb ?: it.image })
            playerUi?.setButtons(buildButtons(this))
        } ?: run {
            state.currentMedia = null
            state.receivedVideoId = null
            youTubePlayer?.pause()
            playerUi?.reset()
            playerUi?.setPlaylistItem(null)
            playerUi?.setPlaylistName("No Item")
            playerUi?.setButtons(buildButtons(null))
        }
    }

    private fun buildButtons(item: PlaylistItemDomain?) = PlayerContract.View.Model.Buttons(
        nextTrackEnabled = queue.playlist
            ?.run { currentIndex < (queue.playlist?.items?.size ?: 0) - 1 }
            ?: false,
        prevTrackEnabled = queue.playlist?.run { currentIndex > 0 } ?: false,
        seekEnabled = item != null && (item.media.duration != null && !item.media.isLiveOrUpcoming()),
    )//.also { log.e("buttons: seekbar:${it.seekEnabled} dur: ${item?.media?.duration} live:${item?.media.isLiveOrUpcoming()}") }

    private fun setupPlayer(controls: PlayerContract.PlayerControls) {
        controls.apply {
            addListener(this@YouTubePlayerListener)
            setTitle(state.currentMedia?.title ?: "No Media")
            setPlayerState(state.playState)
            setDuration(state.durationSec)
            setCurrentSecond(state.positionSec)
            if (state.currentMedia == null) {
                state.currentMedia = queue.currentItem?.media
            }
            setPlaylistItem(queue.currentItem)
        }
    }

    private fun cleanupPlayer(controls: PlayerContract.PlayerControls?) {
        controls?.apply {
            removeListener(this@YouTubePlayerListener)
            reset()
        }
    }

    companion object {
        private const val UI_UPDATE_INTERVAL = 500
        private const val DB_UPDATE_INTERVAL = 3000
    }
}
