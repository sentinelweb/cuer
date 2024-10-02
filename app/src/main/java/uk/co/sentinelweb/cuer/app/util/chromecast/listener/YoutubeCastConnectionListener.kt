package uk.co.sentinelweb.cuer.app.util.chromecast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.State.TargetDetails
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ControlTarget.ChromeCast
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class YoutubeCastConnectionListener constructor(
    private val state: State,
    private val creator: YoutubePlayerContextCreator,
    private val mediaSessionManager: MediaSessionContract.Manager,
    private val castWrapper: ChromecastContract.Wrapper,
    private val queue: QueueMediatorContract.Consumer,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
) : ChromecastConnectionListener {

    data class State(
        var connectionState: PlayerContract.CastConnectionState? = null,
    )

    private var context: ChromecastYouTubePlayerContext? = null
    private var youTubePlayerListener: YouTubePlayerListener? = null

    private lateinit var playlistFlowJob: Job

    fun setContext(context: ChromecastYouTubePlayerContext) {
        this.context = context
        playlistFlowJob = queue.currentPlaylistFlow
            .onEach { updateFromQueue() }
            .launchIn(coroutines.mainScope)
        if (state.connectionState == Disconnected) {
            setupPlayerListener()
            youTubePlayerListener?.apply { mediaSessionManager.checkCreateMediaSession(this) }
            updateFromQueue()
        }
    }

    var playerUi: PlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            field = value
            youTubePlayerListener?.playerUi = field
            field?.let {
                restoreState()
            }
        }

    override fun onChromecastConnecting() {
        state.connectionState = Connecting
        playerUi?.setCastDetails(TargetDetails(ChromeCast, Connecting))
    }

    override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        state.connectionState = Connected
        playerUi?.setCastDetails(TargetDetails(ChromeCast, Connected, castWrapper.getCastDeviceName()))

        youTubePlayerListener?.let {
            it.playerUi = playerUi
            mediaSessionManager.checkCreateMediaSession(it)
        } ?: run {
            context?.apply { setupPlayerListener() }
        }

        updateFromQueue()
    }

    private fun setupPlayerListener() {
        youTubePlayerListener = creator.createPlayerListener().also {
            context?.initialize(it) ?: throw IllegalStateException("context has not been initialised")
            mediaSessionManager.checkCreateMediaSession(it)
            it.playerUi = playerUi
        }
    }

    override fun onChromecastDisconnected() {
        state.connectionState = Disconnected
        playerUi?.setCastDetails(TargetDetails(ChromeCast, Disconnected))

        youTubePlayerListener?.onDisconnected()
        youTubePlayerListener = null
        mediaSessionManager.destroyMediaSession()
    }

    private fun restoreState() {
        state.connectionState?.apply {
            playerUi?.setCastDetails(
                TargetDetails(
                    ChromeCast,
                    this,
                    castWrapper.getCastDeviceName()
                )
            )
        }
        updateFromQueue()
    }

    fun isConnected(): Boolean {
        return state.connectionState == Connected
    }

    fun destroy() {
        playlistFlowJob.cancel()
        castWrapper.killCurrentSession()
        coroutines.cancel()
    }

    private fun updateFromQueue() {
        if (queue.currentItem != null) {
            playerUi?.apply {
                if (youTubePlayerListener != null) {
                    queue.currentItem?.apply {
                        setCurrentSecond((media.positon?.toFloat() ?: 0f) / 1000f)
                    }
                }
                queue.currentItem?.let { setPlaylistItem(it) }
                setPlaylistName(queue.playlist?.title ?: "none")
                setPlaylistImage(queue.playlist?.let { it.thumb ?: it.image })
            }
        } else {
            coroutines.mainScope.launch {
                delay(100)
                updateFromQueue()
            }
        }
    }
}
