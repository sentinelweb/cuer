package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ConnectionState.*
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

class YoutubeCastConnectionListener constructor(
    private val state: State,
    private val creator: YoutubePlayerContextCreator,
    private val mediaSessionManager: MediaSessionContract.Manager,
    private val castWrapper: ChromeCastWrapper,
    private val queue: QueueMediatorContract.Consumer,
    private val coroutines: CoroutineContextProvider,
) : ChromecastConnectionListener {

    data class State constructor(
        var connectionState: PlayerContract.ConnectionState? = null,
    )

    private var context: ChromecastYouTubePlayerContext? = null
    private var youTubePlayerListener: YouTubePlayerListener? = null

    init {
        queue.currentPlaylistFlow
            .onEach { updateFromQueue() }
            .launchIn(coroutines.mainScope)
    }

    fun setContext(context: ChromecastYouTubePlayerContext) {
        this.context = context
        if (state.connectionState == CC_CONNECTED) {
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
        state.connectionState = CC_CONNECTING.also {
            playerUi?.setConnectionState(it)
        }
    }

    override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        state.connectionState = CC_CONNECTED.also {
            playerUi?.setConnectionState(it)
        }
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
        state.connectionState = CC_DISCONNECTED.also {
            playerUi?.setConnectionState(it)
        }
        youTubePlayerListener?.onDisconnected()
        youTubePlayerListener = null
        mediaSessionManager.destroyMediaSession()
    }

    private fun restoreState() {
        state.connectionState?.apply { playerUi?.setConnectionState(this) }
        coroutines.mainScope.launch {
            queue.emitState()
        }
    }

    fun isConnected(): Boolean {
        return state.connectionState == CC_CONNECTED
    }

    fun destroy() {
        castWrapper.killCurrentSession()
        coroutines.cancel()
    }

    private fun updateFromQueue() {
        playerUi?.apply {
            if (youTubePlayerListener != null) {
                queue.currentItem?.apply {
                    setPlayerState(PlayerStateDomain.PAUSED)
                    setCurrentSecond((media.positon?.toFloat() ?: 0f) / 1000f)
                }
            }
            queue.currentItem?.let { setPlaylistItem(it, queue.source) }
            setPlaylistName(queue.playlist?.title ?: "none")
            setPlaylistImage(queue.playlist?.let { it.thumb ?: it.image })
        }
    }

}