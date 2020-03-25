package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.PlayerStateUi
import uk.co.sentinelweb.cuer.ui.queue.dummy.Queue

class YouTubePlayerListener constructor(
    private val playerUi: CastPlayerContract.PresenterExternal
) : AbstractYouTubePlayerListener(), CastPlayerContract.PresenterExternal.Listener {

    private var youTubePlayer: YouTubePlayer? = null
    val idProvider = Queue.VideoIdProvider() // todo remove & make queue interface
    init {
        playerUi.addListener(this)
    }

    fun cleanup() {
        playerUi.removeListener(this)
        playerUi.reset()
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        this.youTubePlayer = youTubePlayer
        youTubePlayer.loadVideo(idProvider.getNextVideoId(), 0f)
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {
        this.youTubePlayer = youTubePlayer
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        playerUi.setCurrentSecond(second)
    }

    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerError) {
        playerUi.error(error.toString()) // todo map errors
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, q: PlaybackQuality) {}

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, r: PlaybackRate) {}

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerState) {
        when (state) {
            PlayerState.ENDED -> playerUi.setPlayerState(PlayerStateUi.ENDED)
            PlayerState.PAUSED -> playerUi.setPlayerState(PlayerStateUi.PAUSED)
            PlayerState.PLAYING -> playerUi.setPlayerState(PlayerStateUi.PLAYING)
            PlayerState.BUFFERING -> playerUi.setPlayerState(PlayerStateUi.BUFFERING)
            PlayerState.UNSTARTED -> playerUi.setPlayerState(PlayerStateUi.UNSTARTED)//resetUi()
            PlayerState.UNKNOWN -> playerUi.setPlayerState(PlayerStateUi.UNKNOWN)
            PlayerState.VIDEO_CUED -> playerUi.setPlayerState(PlayerStateUi.VIDEO_CUED)
        }
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        playerUi.setDuration(duration)
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}

    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}

    override fun playPressed() {
        youTubePlayer?.play()
    }

    override fun pausePressed() {
        youTubePlayer?.pause()
    }

    override fun seekBackPressed() {}

    override fun seekFwdPressed() {}

    override fun trackBackPressed() {
        youTubePlayer?.loadVideo(idProvider.getPreviousVideoId(), 0f)
    }

    override fun trackFwdPressed() {
        youTubePlayer?.loadVideo(idProvider.getNextVideoId(), 0f)
    }

    override fun onSeekChanged(ratio: Float) {}
}