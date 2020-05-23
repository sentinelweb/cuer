package uk.co.sentinelweb.cuer.app.service.cast

import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder

class YoutubeCastServiceController constructor(
    private val service: YoutubeCastService,
    private val state: YoutubeCastServiceState,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val notification: PlayerControlsNotificationContract.PresenterExternal,
    private val appState: CuerAppState
) {
    fun initialise() {
        ytContextHolder.playerUi = notification
    }

    fun handleAction(action: String?) {
        notification.handleAction(action)
    }

    fun destroy() {
        notification.destroy()
        if (ytContextHolder.playerUi == notification) {
            ytContextHolder.destroy()
        }
    }

    companion object {
    }
}