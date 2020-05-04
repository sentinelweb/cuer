package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class YouTubePlayerListener(
    private val state: YouTubePlayerListenerState,
    private val queue: QueueMediatorContract.Mediator,
    private val mediaSessionManager: MediaSessionManager,
    private val log: LogWrapper
) : AbstractYouTubePlayerListener(),
    CastPlayerContract.PlayerControls.Listener,
    QueueMediatorContract.ConsumerListener {

    private var youTubePlayer: YouTubePlayer? = null

    var playerUi: CastPlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            value?.let {
                setupPlayer(it)
            } ?: cleanupPlayer(field)
            field = value
        }

    init {
        log.tag = "YouTubePlayer"
        queue.addConsumerListener(this)
    }

    fun onDisconnected() {
        youTubePlayer?.removeListener(this)
        youTubePlayer = null
        playerUi?.reset()
        playerUi = null
        queue.removeConsumerListener(this)
    }

    // region AbstractYouTubePlayerListener
    override fun onReady(youTubePlayer: YouTubePlayer) {
        this.youTubePlayer = youTubePlayer
        loadVideo(queue.getCurrentItem())
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {
        this.youTubePlayer = youTubePlayer
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        this.youTubePlayer = youTubePlayer
        state.positionSec = second
        playerUi?.setCurrentSecond(second)
        state.currentMedia = state.currentMedia?.copy(positon = (second * 1000).toLong())
        mediaSessionManager.updatePlaybackState(state.currentMedia, state.playState)
    }

    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerError) {
        this.youTubePlayer = youTubePlayer
        playerUi?.error(error.toString()) // todo map errors
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
        playerState: PlayerState
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
        mediaSessionManager.updatePlaybackState(state.currentMedia, state.playState)
        if (state.playState == PlayerStateDomain.ENDED) {
            queue.onTrackEnded(state.currentMedia)
        }
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        this.youTubePlayer = youTubePlayer
        state.durationSec = duration
        playerUi?.setDuration(duration)
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
        this.youTubePlayer = youTubePlayer
        log.d("Got id: $videoId")
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
            queue.lastItem()
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
    // endregion

    // region  QueueMediatorContract.ConsumerListener
    override fun onItemChanged() {
        loadVideo(queue.getCurrentItem())
    }
    // endregion

    private fun handleError(e: Exception) {
        playerUi?.error("Error: ${e.message ?: "Unknown - check log"}")
        log.e("Error playing", e)
    }

    private fun loadVideo(item: PlaylistItemDomain?) {
        item?.let {
            youTubePlayer?.loadVideo(item.media.mediaId, 0f)
            updateStateForMedia(item)
        } ?: playerUi?.reset()
    }

    private fun updateStateForMedia(item: PlaylistItemDomain) {
        state.currentMedia = item.media
        playerUi?.setMedia(item.media)
    }

    // todo fix this - not clean
    private fun setupPlayer(controls: CastPlayerContract.PlayerControls) {
        controls.addListener(this)
        controls.setTitle(state.currentMedia?.title ?: "No Media")
        controls.setPlayerState(state.playState)
        controls.setDuration(state.durationSec)
        controls.setCurrentSecond(state.positionSec)
        state.currentMedia?.apply { controls.setMedia(this) }
    }

    private fun cleanupPlayer(controls: CastPlayerContract.PlayerControls?) {
        controls?.removeListener(this)
        controls?.reset()
    }
}
