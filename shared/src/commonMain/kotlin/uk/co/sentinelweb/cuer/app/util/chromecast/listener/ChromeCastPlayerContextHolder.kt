package uk.co.sentinelweb.cuer.app.util.chromecast.listener

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract

interface ChromeCastPlayerContextHolder {

    var playerUi: PlayerContract.PlayerControls?

    fun create()

    fun isCreated(): Boolean

    fun isConnected(): Boolean

    fun onDisconnected()

    fun destroy()

}