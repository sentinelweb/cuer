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
    var playListId: OrchestratorContract.Identifier<*> = (-1L).toLocalIdentifier()
    lateinit var callback: (PlaylistContract.CastState) -> Unit

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
                callback(PlaylistContract.CastState.PLAYING)
            }
        }

        override fun onChromecastConnecting() {
            if (isQueuedPlaylist) {
                callback(PlaylistContract.CastState.CONNECTING)
            }
        }

        override fun onChromecastDisconnected() {
            if (isQueuedPlaylist) {
                callback(PlaylistContract.CastState.NOT_CONNECTED)
            }
        }
    }
}