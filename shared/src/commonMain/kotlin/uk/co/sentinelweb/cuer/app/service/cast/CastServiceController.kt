package uk.co.sentinelweb.cuer.app.service.cast

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_DISCONNECT
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_STAR
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_STOP
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_VOL_DOWN
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_VOL_MUTE
import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract.Companion.ACTION_VOL_UP
import uk.co.sentinelweb.cuer.app.service.cast.notification.player.PlayerControlsNotificationContract
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.usecase.StarMediaUseCase
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class CastServiceController(
    private val service: CastServiceContract.Service,
    private val notification: PlayerControlsNotificationContract.External,
    private val castController: CastController,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
    private val starMediaUseCase: StarMediaUseCase,
) : CastServiceContract.Controller {

    init {
        log.tag(this)
    }

    override fun initialise() {
        castController.initialiseForService()
    }

    override fun handleAction(action: String?, extrasToMap: Map<String, Any>?) {
        when (action) {
            ACTION_DISCONNECT -> {
                castController.killCurrentSession()
                service.stopSelf()
            }

            ACTION_STOP ->
                coroutines.mainScope.launch {
                    castController.stopCuerCast()
                    service.stopSelf()
                }

            ACTION_STAR -> {
                //log.d("star: ${service.toString()}")
                starMediaUseCase.starMedia()
            }

            ACTION_VOL_DOWN ->
                castController.decrementVolume()

            ACTION_VOL_UP ->
                castController.incrementVolume()

            ACTION_VOL_MUTE ->
                castController.setVolume(0f)


            else ->
                notification.handleAction(action)
        }
    }

    override fun destroy() {
        notification.destroy()
        castController.onServiceDestroy()
    }

}
