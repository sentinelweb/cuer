package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain

interface FloatingPlayerContract {

    interface Manager {
        fun get(): Service?
        fun isRunning(): Boolean
        fun playItem(item: PlaylistAndItemDomain)
    }

    interface Service {
        val external: External
        fun stopSelf()
    }

    interface Controller {
        val external: External
        fun initialise()
        fun destroy()
        fun handleAction(intent: Any) // todo wrap intent?
    }

    interface External {
        var mainPlayerControls: PlayerContract.PlayerControls?
    }
}