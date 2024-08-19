package uk.co.sentinelweb.cuer.app.util.chromecast.listener

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract

interface ChromecastContract {
    interface PlayerContextHolder {

        var mainPlayerControls: PlayerContract.PlayerControls?

        fun create(playerControls: PlayerContract.PlayerControls)

        fun isCreated(): Boolean

        fun isConnected(): Boolean

        fun destroy()
    }

    interface DialogWrapper {
        fun showRouteSelector()
    }

    interface Wrapper {
        fun killCurrentSession()
        fun getCastDeviceName(): String?
        fun logRoutes()
        fun getVolume(): Double
        fun getMaxVolume(): Double
        fun logCastDevice()
        fun setVolume(volume: Float)
        fun getMediaRouteIdForCurrentSession(): String?
    }
}
