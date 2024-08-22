package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.State.TargetDetails
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface PlayerControlsNotificationContract {

    interface External {
        fun handleAction(action: String?)
        fun destroy()
        fun setIcon(icon: Int)
        fun setBlocked(blocked: Boolean)
    }

    interface Controller {

    }

    interface View {
        fun showNotification(
            state: State
        )

        fun stopSelf()
        fun setIcon(icon: Int)
    }

    data class State(
        var playState: PlayerStateDomain = PlayerStateDomain.UNKNOWN,
        var positionMs: Long = 0,
        var seekPositionMs: Long = 0,
        var durationMs: Long = 0,
        var title: String = "",
        var item: PlaylistItemDomain? = null,
        var bitmap: Any? = null,
        var playlistName: String = "none",
        var blocked: Boolean = false,
        var seekEnabled: Boolean = true,
        var nextEnabled: Boolean = true,
        var prevEnabled: Boolean = true,
        var targetDetails: TargetDetails = TargetDetails(),
        var lastNotificationShowTime: Long = 0,
        var volumeFraction: Float = 0f, // 0f .. 2f
    )
}
