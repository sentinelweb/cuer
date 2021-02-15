package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.ConnectionState
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.ConnectionState.*
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

class YoutubeCastConnectionListener constructor(
    private val creator: YoutubePlayerContextCreator,
    private val mediaSessionManager: MediaSessionManager,
    private val castWrapper: ChromeCastWrapper,
    private val queue: QueueMediatorContract.Consumer,
    private val coroutines: CoroutineContextProvider
) : ChromecastConnectionListener {

    lateinit var context: ChromecastYouTubePlayerContext
    private var youTubePlayerListener: YouTubePlayerListener? = null
    private var connectionState: ConnectionState? = null

    init {
        queue.currentPlaylistFlow
            .onEach { pushUpdateFromQueue() }
            .launchIn(coroutines.mainScope)
    }

    var playerUi: CastPlayerContract.PlayerControls? = null
        get() = field
        set(value) {
            field = value
            youTubePlayerListener?.playerUi = field
            field?.let {
                restoreState()
            }
        }

    override fun onChromecastConnecting() {
        connectionState = CC_CONNECTING.also {
            playerUi?.setConnectionState(it)
        }
    }

    override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        connectionState = CC_CONNECTED.also {
            playerUi?.setConnectionState(it)
        }
        youTubePlayerListener?.let {
            it.playerUi = playerUi
        } ?: setupPlayerListener() // todo crash here https://github.com/sentinelweb/cuer/issues/137

        mediaSessionManager.checkCreateMediaSession(youTubePlayerListener!!)
        pushUpdateFromQueue()
    }

    private fun setupPlayerListener() {
        youTubePlayerListener = creator.createPlayerListener().also {
            context.initialize(it)
            it.playerUi = playerUi
        }
    }

    override fun onChromecastDisconnected() {
        connectionState = CC_DISCONNECTED.also {
            playerUi?.setConnectionState(it)
        }
        youTubePlayerListener?.onDisconnected()
        youTubePlayerListener = null
        mediaSessionManager.destroyMediaSession()
    }

    private fun restoreState() {
        connectionState?.apply { playerUi?.setConnectionState(this) }
    }

    fun isConnected(): Boolean {
        return connectionState == CC_CONNECTED
    }

    fun destroy() {
        castWrapper.killCurrentSession()
        coroutines.cancel()
    }

    private fun pushUpdateFromQueue() {
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