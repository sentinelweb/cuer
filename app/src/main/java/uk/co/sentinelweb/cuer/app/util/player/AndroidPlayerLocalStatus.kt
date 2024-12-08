package uk.co.sentinelweb.cuer.app.util.player

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract

class AndroidPlayerLocalStatus : PlayerContract.LocalStatus {
    override fun isPlayerActive(): Boolean = false
    override fun playerStatus(): PlayerSessionContract.PlayerStatusMessage {
        TODO("Not yet implemented")
    }
}
