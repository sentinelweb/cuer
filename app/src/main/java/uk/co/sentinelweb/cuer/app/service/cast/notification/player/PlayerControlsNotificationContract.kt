package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.graphics.Bitmap
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

interface PlayerControlsNotificationContract {

    interface PresenterExternal : CastPlayerContract.PlayerControls {
        fun handleAction(action: String?)
        fun destroy()
    }

    interface Presenter {

    }

    interface View {
        fun showNotification(
            state: PlayerStateDomain,
            media: MediaDomain?,
            bitmap: Bitmap?
        )
    }

}