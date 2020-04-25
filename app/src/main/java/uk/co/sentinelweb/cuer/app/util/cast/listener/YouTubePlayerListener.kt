package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.PlayerStateUi
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class YouTubePlayerListener(
    private val state: YouTubePlayerListenerState,
    private val queue: QueueMediatorContract.Mediator
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
        queue.addConsumerListener(this)
    }

    // todo fix this - not clean
    private fun setupPlayer(it: CastPlayerContract.PlayerControls) {
        it.addListener(this)
        it.setTitle(state.currentMedia?.title ?: "No Media")
        it.setPlayerState(state.playState)
        it.setDuration(state.durationSec)
        it.setCurrentSecond(state.positionSec)
    }

    private fun cleanupPlayer(it: CastPlayerContract.PlayerControls?) {
        it?.removeListener(this)
        it?.reset()
    }

    fun onDisconnected() {
        youTubePlayer?.removeListener(this)
        youTubePlayer = null
        playerUi?.reset()
        playerUi = null
        queue.removeConsumerListener(this)
    }

    private fun loadVideo(item: PlaylistItemDomain?) {
        item?.let {
            youTubePlayer?.loadVideo(item.media.mediaId, 0f)
            updateStateForMedia(item)
        } ?: playerUi?.reset()
    }

    private fun updateStateForMedia(item: PlaylistItemDomain): Unit? {
        state.currentMedia = item.media
        val displayTitle = item.media.title ?: item.media.url
        return playerUi?.setTitle(displayTitle)
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
        if (state.playState == PlayerStateUi.ENDED) {
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
        queue.lastItem()
    }

    override fun trackFwd() {
        queue.nextItem()
    }

    override fun seekTo(positionMs: Long) {
        youTubePlayer?.seekTo(positionMs / 1000f)
    }
    // endregion

    // region  QueueMediatorContract.ConsumerListener
    override fun onItemChanged() {
        loadVideo(queue.getCurrentItem())
    }
    // endregion
}