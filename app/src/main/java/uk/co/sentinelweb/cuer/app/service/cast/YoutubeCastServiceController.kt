package uk.co.sentinelweb.cuer.app.service.cast

import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder

class YoutubeCastServiceController constructor(
    private val service: YoutubeCastService,
    private val state: YoutubeCastServiceState,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val notification: PlayerControlsNotificationContract.Presenter
) {
    fun initialise() {
        notification.show()
    }

    fun handleAction(action: String?) {
        if (ACTION_PAUSE == action) {
            //controller.pause()
            // notification.
        }
    }

    fun destroy() {
//        wrapper = null
//        state.youtubePlayerContext?.destroy()
//        state.youtubePlayerContext = null
    }

    fun pause() {
    }

    companion object {
        private const val NOTIF_ID = 34564
        private const val ACTION_PAUSE = "pause"
    }
}