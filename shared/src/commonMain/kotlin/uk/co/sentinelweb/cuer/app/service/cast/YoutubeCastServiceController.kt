package uk.co.sentinelweb.cuer.app.service.cast

import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceContract.Companion.ACTION_DISCONNECT
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceContract.Companion.ACTION_STAR
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class YoutubeCastServiceController(
    private val service: YoutubeCastServiceContract.Service,
    private val notification: PlayerControlsNotificationContract.External,
//    private val chromeCastWrapper: ChromecastContract.Wrapper,
    private val castController: CastController,
    private val log: LogWrapper,
) : YoutubeCastServiceContract.Controller {
    init {
        log.tag(this)
    }

    override fun initialise() {
        // todo set into castcontroller (create inject from service di config)
        //ytContextHolder.playerUi = notification
        castController.initialiseForService()
    }

    override fun handleAction(action: String?) {
        when (action) {
            ACTION_DISCONNECT -> {
                // todo unify this in cast controller

                //chromeCastWrapper.killCurrentSession()
                castController.killCurrentSession()
                service.stopSelf()
            }
            ACTION_STAR ->
//                log.d(serviceWrapper.getServiceData(YoutubeCastService::class.java.name).toString())
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