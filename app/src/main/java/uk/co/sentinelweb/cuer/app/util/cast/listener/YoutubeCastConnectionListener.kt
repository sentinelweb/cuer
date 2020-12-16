package uk.co.sentinelweb.cuer.app.util.cast.listener

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.ConnectionState
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.ConnectionState.*
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

class YoutubeCastConnectionListener constructor(
    private val creator: YoutubePlayerContextCreator,
    private val mediaSessionManager: MediaSessionManager,
    private val castWrapper: ChromeCastWrapper,
    private val queue: QueueMediatorContract.Consumer
) : ChromecastConnectionListener, QueueMediatorContract.ConsumerListener {

    private var youTubePlayerListener: YouTubePlayerListener? = null
    private var connectionState: ConnectionState? = null

    init {
        queue.addConsumerListener(this)
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
        //connectionMonitor.setTimer({ connectionState })
        connectionState = CC_CONNECTING.also {
            playerUi?.setConnectionState(it)
            //connectionMonitor.connectionState = it
        }
    }

    override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        //if (connectionMonitor.checkAlreadyConnected(connectionState)) return

        connectionState = CC_CONNECTED.also {
            playerUi?.setConnectionState(it)
            //connectionMonitor.connectionState = it
        }
        youTubePlayerListener?.let {
            it.playerUi = playerUi
        } ?: setupPlayerListener(chromecastYouTubePlayerContext)

        youTubePlayerListener?.apply { mediaSessionManager.createMediaSession(this) }
    }

    private fun setupPlayerListener(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        youTubePlayerListener = creator.createPlayerListener().also {
            chromecastYouTubePlayerContext.initialize(it)
            it.playerUi = playerUi
        }
    }

    override fun onChromecastDisconnected() {
        connectionState = CC_DISCONNECTED.also {
            playerUi?.setConnectionState(it)
            //connectionMonitor.connectionState = it
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
        //connectionMonitor.cancelTimer()
        castWrapper.killCurrentSession()
    }

    override fun onItemChanged() {
    }

    override fun onPlaylistUpdated() {
        playerUi?.apply {
            queue.currentItem?.media?.apply {
                if (youTubePlayerListener == null) {
                    setPlayerState(PlayerStateDomain.PAUSED)
                    setCurrentSecond((positon?.toFloat() ?: 0f) / 1000f)
                }
                setMedia(this)
            }
            setPlaylistName(queue.playlist?.title ?: "none")
            setPlaylistImage(queue.playlist?.let { it.thumb ?: it.image })
        }
    }

}