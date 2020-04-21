package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.PlayerStateUi
import uk.co.sentinelweb.cuer.ui.queue.dummy.Queue

class YouTubePlayerListener constructor(
    private val state: YouTubePlayerListenerState
) : AbstractYouTubePlayerListener(),
    CastPlayerContract.PlayerControls.Listener {

    private var youTubePlayer: YouTubePlayer? = null
    val idProvider = Queue.VideoProvider() // todo remove & make queue interface
    var currentItem: Queue.QueueItem? = null // todo move to player?

    var playerUi: CastPlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            value?.let {
                setupPlayer(it)
            } ?: cleanupPlayer(field)
            field = value
        }

    // todo fix this - not clean
    private fun setupPlayer(it: CastPlayerContract.PlayerControls) {
        it.addListener(this)
        it.setTitle(state.title)
        it.setPlayerState(state.playState)
        it.setDuration(state.durationSec)
        it.setCurrentSecond(state.positionSec)
    }

    private fun cleanupPlayer(it: CastPlayerContract.PlayerControls?) {
        it?.removeListener(this)
        it?.reset()
    }

    // region AbstractYouTubePlayerListener
    override fun onReady(youTubePlayer: YouTubePlayer) {
        this.youTubePlayer = youTubePlayer
        currentItem = idProvider.getNextVideo()
        loadCurrentVideo()
    }

    private fun loadCurrentVideo() {
        youTubePlayer?.loadVideo(currentItem?.getId()!!, 0f)
        state.title = currentItem?.title ?: currentItem?.url ?: ""
        playerUi?.setTitle(state.title)
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {
        this.youTubePlayer = youTubePlayer
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        this.youTubePlayer = youTubePlayer
        state.positionSec = second
        playerUi?.setCurrentSecond(second)
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
            PlayerState.ENDED -> PlayerStateUi.ENDED
            PlayerState.PAUSED -> PlayerStateUi.PAUSED
            PlayerState.PLAYING -> PlayerStateUi.PLAYING
            PlayerState.BUFFERING -> PlayerStateUi.BUFFERING
            PlayerState.UNSTARTED -> PlayerStateUi.UNSTARTED
            PlayerState.UNKNOWN -> PlayerStateUi.UNKNOWN
            PlayerState.VIDEO_CUED -> PlayerStateUi.VIDEO_CUED
        }
        playerUi?.setPlayerState(state.playState)
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        this.youTubePlayer = youTubePlayer
        state.positionSec = duration
        playerUi?.setDuration(duration)
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
        this.youTubePlayer = youTubePlayer
        // todo get video info
    }

    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
        this.youTubePlayer = youTubePlayer
    }
    // endregion

    // region  CastPlayerContract.PresenterExternal.Listener
    override fun play() {
        youTubePlayer?.play()
    }

    override fun pause() {
        youTubePlayer?.pause()
    }

    override fun trackBack() {
        currentItem = idProvider.getPreviousVideo()
        loadCurrentVideo()
    }

    override fun trackFwd() {
        currentItem = idProvider.getNextVideo()
        loadCurrentVideo()
    }

    override fun seekTo(positionMs: Long) {
        youTubePlayer?.seekTo(positionMs / 1000f)
    }
    // endregion

    fun onDisconnected() {
        youTubePlayer?.removeListener(this)
        youTubePlayer = null
        playerUi?.reset()
        playerUi = null
    }
}