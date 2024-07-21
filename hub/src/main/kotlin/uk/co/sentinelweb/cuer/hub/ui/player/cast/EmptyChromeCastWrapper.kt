package uk.co.sentinelweb.cuer.hub.ui.player.cast

import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract

class EmptyChromeCastWrapper : ChromecastContract.Wrapper {
    override fun killCurrentSession() = Unit

    override fun getCastDeviceName(): String = "Empty"
}