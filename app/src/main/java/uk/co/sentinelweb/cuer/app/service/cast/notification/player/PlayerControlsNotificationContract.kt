package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import android.graphics.Bitmap
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

interface PlayerControlsNotificationContract {

    interface External : PlayerContract.PlayerControls {
        fun handleAction(action: String?)
        fun destroy()
    }

    interface Controller {

    }

    interface View {
        fun showNotification(
            state: PlayerStateDomain,
            media: MediaDomain?,
            bitmap: Bitmap?
        )

        fun stopSelf()
    }

    data class State(
        var playState: PlayerStateDomain = PlayerStateDomain.UNKNOWN,
        var positionMs: Long = 0,
        var seekPositionMs: Long = 0,
        var durationMs: Long = 0,
        var title: String = "",
        var media: MediaDomain? = null,
        var bitmap: Bitmap? = null,
        var playlistName: String = "none"
    )
}