package uk.co.sentinelweb.cuer.app.util.cast.listener

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract

interface CastPlayerContextHolder {

    var playerUi: PlayerContract.PlayerControls?

    fun create()

    fun isCreated(): Boolean

    fun isConnected(): Boolean

    fun onDisconnected()

    fun destroy()

}