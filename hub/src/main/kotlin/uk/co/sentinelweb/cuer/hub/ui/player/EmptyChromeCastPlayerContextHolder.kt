package uk.co.sentinelweb.cuer.hub.ui.player

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromeCastPlayerContextHolder

class EmptyChromeCastPlayerContextHolder : ChromeCastPlayerContextHolder {
    override var playerUi: PlayerContract.PlayerControls?
        get() = null
        set(value) {}

    override fun create() = Unit

    override fun isCreated(): Boolean = false

    override fun isConnected(): Boolean = false

    override fun onDisconnected() = Unit

    override fun destroy() = Unit
}