package uk.co.sentinelweb.cuer.app.ui.playlist

import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.toLocalIdentifier
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder

class QueueCastConnectionListener(
    private val queue: QueueMediatorContract.Producer,
    private val ytCastContextHolder: ChromecastYouTubePlayerContextHolder
) {

    enum class CastState { PLAYING, CONNECTING, NOT_CONNECTED }

    var playListId: OrchestratorContract.Identifier<*> = "".toLocalIdentifier()
    lateinit var callback: (CastState) -> Unit

    private val isQueuedPlaylist: Boolean
        get() = playListId == queue.playlistId

    fun isPlaylistPlaying() = isQueuedPlaylist && ytCastContextHolder.isConnected()

    fun listenForState() {
        ytCastContextHolder.addConnectionListener(castConnectionListener)
    }

    fun unlistenForState() {
        ytCastContextHolder.removeConnectionListener(castConnectionListener)
    }

    private val castConnectionListener = object : ChromecastConnectionListener {
        override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
            if (isQueuedPlaylist) {
                callback(CastState.PLAYING)
            }
        }

        override fun onChromecastConnecting() {
            if (isQueuedPlaylist) {
                callback(CastState.CONNECTING)
            }
        }

        override fun onChromecastDisconnected() {
            if (isQueuedPlaylist) {
                callback(CastState.NOT_CONNECTED)
            }
        }
    }
}