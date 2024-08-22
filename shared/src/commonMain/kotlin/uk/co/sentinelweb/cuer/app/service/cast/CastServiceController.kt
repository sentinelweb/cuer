package uk.co.sentinelweb.cuer.app.service.cast

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.service.EXTRA_ITEM_ID
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
import uk.co.sentinelweb.cuer.domain.ext.deserialiseGuidIdentifier

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

    override fun handleAction(action: String?, extras: Map<String, Any>?) {
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
                (extras
                    ?.get(EXTRA_ITEM_ID) as String?)
                    ?.let { deserialiseGuidIdentifier(it) }
                    ?.also { itemId ->
                        coroutines.mainScope.launch {
                            starMediaUseCase.starMedia(itemId = itemId)
                        }
                    }
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
