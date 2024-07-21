package uk.co.sentinelweb.cuer.hub.ui.player.cast

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromeCastContract

class EmptyChromeCastPlayerContextHolder : ChromeCastContract.PlayerContextHolder {
    override var playerUi: PlayerContract.PlayerControls?
        get() = null
        set(value) {}

    override fun create() = Unit

    override fun isCreated(): Boolean = false

    override fun isConnected(): Boolean = false

    override fun onDisconnected() = Unit

    override fun destroy() = Unit
}