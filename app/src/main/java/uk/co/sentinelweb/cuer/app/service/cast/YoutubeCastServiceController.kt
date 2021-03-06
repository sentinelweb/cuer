package uk.co.sentinelweb.cuer.app.service.cast

import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_DISCONNECT
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationController.Companion.ACTION_STAR
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.wrapper.ServiceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class YoutubeCastServiceController constructor(
    private val service: YoutubeCastServiceContract.Service,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val notification: PlayerControlsNotificationContract.External,
    private val chromeCastWrapper: ChromeCastWrapper,
    private val log: LogWrapper,
    private val serviceWrapper: ServiceWrapper
) : YoutubeCastServiceContract.Controller {

    override fun initialise() {
        ytContextHolder.playerUi = notification
    }

    override fun handleAction(action: String?) {
        when (action) {
            ACTION_DISCONNECT -> {
                chromeCastWrapper.killCurrentSession()
                service.stopSelf()
            }
            ACTION_STAR ->
                log.d(serviceWrapper.getServiceData(YoutubeCastService::class.java.name).toString())
            else ->
                notification.handleAction(action)
        }

    }

    override fun destroy() {
        notification.destroy()
        if (ytContextHolder.playerUi == notification) {
            ytContextHolder.destroy()
        }
    }

}