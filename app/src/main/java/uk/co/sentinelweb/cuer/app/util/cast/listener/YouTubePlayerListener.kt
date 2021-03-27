package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.*
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.stringMedia

class YouTubePlayerListener(
    private val state: State,
    private val queue: QueueMediatorContract.Consumer,
    private val mediaSessionManager: MediaSessionManager,
    private val log: LogWrapper,
    private val timeProvider: TimeProvider,
    private val coroutines: CoroutineContextProvider,
    private val prefs: SharedPrefsWrapper<GeneralPreferences>
) : AbstractYouTubePlayerListener(),
    CastPlayerContract.PlayerControls.Listener {

    data class State constructor(
        var playState: PlayerStateDomain = PlayerStateDomain.UNKNOWN,
        var positionSec: Float = 0f,
        var durationSec: Float = 0f,
        var durationObtainedTime: Long = -1,
        var currentMedia: MediaDomain? = null,
        var lastUpdateMedia: Long = -1L,
        var lastUpdateUI: Long = -1L,
        var receivedVideoId: String? = null
    )

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
        log.tag(this)
        queue.currentItemFlow
            .distinctUntilChanged { old, new -> old?.media?.id == new?.media?.id }
            .onEach { loadVideo(it) }
            .launchIn(coroutines.mainScope)
    }

    fun onDisconnected() {
        youTubePlayer?.removeListener(this)
        youTubePlayer = null
        queue.currentItem?.apply {
            playerUi?.setPlaylistItem(this, queue.source)
            playerUi?.setConnectionState(CastPlayerContract.ConnectionState.CC_DISCONNECTED)
            playerUi?.setPlayerState(PlayerStateDomain.PAUSED)
            playerUi?.setCurrentSecond(0f)
        } ?: run {
            playerUi?.reset()
            playerUi = null
        }
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
        log.d("onVideoDuration dur=${state.durationSec} durObTime=${state.durationObtainedTime}")
        this.youTubePlayer = youTubePlayer
        state.currentMedia
            ?.takeIf { it.isLiveBroadcast }
            ?.apply {
                if (state.durationObtainedTime == -1L) {
                    state.durationSec = duration
                    state.durationObtainedTime = timeProvider.currentTimeMillis()
                    state.receivedVideoId?.let { saveLiveDurationPref() }
                }
            }
            ?: run { state.durationSec = duration }
        playerUi?.setDuration(duration)
        updateMedia(false, durSec = duration)
        state.currentMedia?.apply { mediaSessionManager.setMedia(this) }
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        this.youTubePlayer = youTubePlayer
        if (state.positionSec != second) {
            state.positionSec = second
            updateMedia(true, posSec = second)
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
                if (isLiveBroadcast) getLiveOffsetMs() else null
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
        updateMediaSessionManagerPlaybackState()
        if (state.playState == PlayerStateDomain.ENDED) {
            queue.onTrackEnded(state.currentMedia)
        }
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
        this.youTubePlayer = youTubePlayer
        state.receivedVideoId = videoId
        restoreLiveDurationPref(videoId)
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
        if (state.durationObtainedTime > -1) {
            val timeSinceDurationMs = timeProvider.currentTimeMillis() - state.durationObtainedTime
            val currentDurationSec = state.durationSec + (timeSinceDurationMs / 1000f)
            val offsetSec = currentDurationSec - state.positionSec - timeProvider.timeZomeOffsetSecs()
            return (offsetSec * 1000).toLong()
        }
        return -1
    }
    // endregion

    private fun handleError(e: Exception) {
        playerUi?.error("Error: ${e.message ?: "Unknown - check log"}")
        log.e("Error playing", e)
    }

    private fun loadVideo(item: PlaylistItemDomain?) {
        item?.apply {
            val startPos = media.run {
                val position = positon ?: -1
                val duration = duration ?: -1
                if (position > 0 &&
                    duration > 0 && position < duration - 10000
                ) {
                    position / 1000f
                } else {
                    0f
                }
            }
            state.durationObtainedTime = -1
            clearLiveDurationPrefIfNotSame(media.platformId)
            log.d("loadVideo: play position: pos =  $startPos sec")
            youTubePlayer?.loadVideo(media.platformId, startPos)
            state.currentMedia = media
            playerUi?.setPlaylistItem(queue.currentItem, queue.source)
        } ?: run {
            state.currentMedia = null
            state.receivedVideoId = null
            youTubePlayer?.pause()
            playerUi?.reset()
            playerUi?.setPlaylistItem(null, queue.source)
        }
    }

    // todo fix this - not clean
    private fun setupPlayer(controls: CastPlayerContract.PlayerControls) {
        controls.apply {
            addListener(this@YouTubePlayerListener)
            setTitle(state.currentMedia?.title ?: "No Media")
            setPlayerState(state.playState)
            setDuration(state.durationSec)
            setCurrentSecond(state.positionSec)
            if (state.currentMedia == null) {
                state.currentMedia = queue.currentItem?.media
            }
            setPlaylistItem(queue.currentItem, queue.source)
        }
    }

    private fun cleanupPlayer(controls: CastPlayerContract.PlayerControls?) {
        controls?.apply {
            removeListener(this@YouTubePlayerListener)
            reset()
        }
    }

    private fun saveLiveDurationPref() {
        prefs.putLong(LIVE_DURATION_TIME, state.durationObtainedTime)
        prefs.putString(LIVE_DURATION_ID, state.receivedVideoId ?: throw IllegalStateException("Should have id"))
        prefs.putLong(LIVE_DURATION_DURATION, state.durationSec.toLong())
    }

    private fun restoreLiveDurationPref(id: String) {
        if (prefs.getString(LIVE_DURATION_ID, null) == id) {
            state.durationSec = prefs.getLong(LIVE_DURATION_DURATION)?.toFloat() ?: 0f
            state.durationObtainedTime = prefs.getLong(LIVE_DURATION_TIME) ?: -1
            log.d("restored duration")
        } else {
            log.d("did not restore")
        }
    }

    private fun clearLiveDurationPrefIfNotSame(id: String) {
        if (prefs.getString(LIVE_DURATION_ID, null) != id) {
            prefs.remove(LIVE_DURATION_DURATION)
            prefs.remove(LIVE_DURATION_TIME)
            prefs.remove(LIVE_DURATION_ID)
            log.d("cleared duration")
        } else {
            log.d("did not clear duration")
        }
    }

    companion object {
        private const val UI_UPDATE_INTERVAL = 500
        private const val DB_UPDATE_INTERVAL = 3000
    }
}
