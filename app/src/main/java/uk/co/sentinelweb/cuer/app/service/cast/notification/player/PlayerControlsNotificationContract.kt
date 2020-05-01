package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

interface PlayerControlsNotificationContract {

    interface PresenterExternal : CastPlayerContract.PlayerControls {
        fun show()
        fun handleAction(action: String?)
    }

    interface Presenter {

    }

    interface View {
        fun showNotification(state: PlayerStateDomain)
    }

}