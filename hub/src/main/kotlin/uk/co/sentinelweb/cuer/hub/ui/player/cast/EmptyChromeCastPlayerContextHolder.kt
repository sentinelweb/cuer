package uk.co.sentinelweb.cuer.hub.ui.player.cast

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract

class EmptyChromeCastPlayerContextHolder : ChromecastContract.PlayerContextHolder {
    override var mainPlayerControls: PlayerContract.PlayerControls?
        get() = null
        set(value) {}

    override fun create(playerControls: PlayerContract.PlayerControls) = Unit

    override fun isCreated(): Boolean = false

    override fun isConnected(): Boolean = false

    override fun destroy() = Unit
}
