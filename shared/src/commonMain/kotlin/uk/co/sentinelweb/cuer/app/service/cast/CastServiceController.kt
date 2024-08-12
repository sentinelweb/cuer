package uk.co.sentinelweb.cuer.app.service.cast

import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_DISCONNECT
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_STAR
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class CastServiceController(
    private val service: CastServiceContract.Service,
    private val notification: PlayerControlsNotificationContract.External,
    private val castController: CastController,
    private val log: LogWrapper,
) : CastServiceContract.Controller {

    init {
        log.tag(this)
    }

    override fun initialise() {
        castController.initialiseForService()
    }

    override fun handleAction(action: String?) {
        when (action) {
            ACTION_DISCONNECT -> {
                castController.killCurrentSession()
                service.stopSelf()
            }
            ACTION_STAR ->
                log.d("star: ${service.toString()}")
            else ->
                notification.handleAction(action)
        }
    }

    override fun destroy() {
        notification.destroy()
        castController.onServiceDestroy()
    }

}
