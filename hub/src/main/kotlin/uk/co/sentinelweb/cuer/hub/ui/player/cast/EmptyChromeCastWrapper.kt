package uk.co.sentinelweb.cuer.hub.ui.player.cast

import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract

class EmptyChromeCastWrapper : ChromecastContract.Wrapper {
    override fun killCurrentSession() = Unit

    override fun getCastDeviceName(): String = "Empty"

    override fun logRoutes() = Unit

    override fun getVolume(): Double = 0.0

    override fun getMaxVolume(): Double = 0.0

    override fun logCastDevice() = Unit

    override fun setVolume(volume: Float) = Unit

    override fun getMediaRouteIdForCurrentSession(): String? = null

    override fun getMediaRouteForCurrentSession(): ChromecastContract.Route? = null

    override fun isCastConnected(): Boolean = false
}
