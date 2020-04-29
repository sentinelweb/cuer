package uk.co.sentinelweb.cuer.app.service.cast

import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder

class YoutubeCastServiceController constructor(
    private val service: YoutubeCastService,
    private val state: YoutubeCastServiceState,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val notification: PlayerControlsNotificationContract.PresenterExternal
) {
    fun initialise() {
        notification.show()
        ytContextHolder.get()!!.playerUi = notification
    }

    fun handleAction(action: String?) {
        notification.handleAction(action)
    }

    fun destroy() {
//        wrapper = null
//        state.youtubePlayerContext?.destroy()
//        state.youtubePlayerContext = null
    }

    companion object {
    }
}