package uk.co.sentinelweb.cuer.app.util.chromecast.listener

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract

interface ChromeCastContract {
    interface PlayerContextHolder {

        var playerUi: PlayerContract.PlayerControls?

        fun create()

        fun isCreated(): Boolean

        fun isConnected(): Boolean

        fun onDisconnected()

        fun destroy()
    }

    interface DialogWrapper {
        fun showRouteSelector()
    }

    interface Wrapper {
        fun killCurrentSession()
        fun getCastDeviceName(): String?
    }
}