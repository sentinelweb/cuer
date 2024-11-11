package uk.co.sentinelweb.cuer.app.util.player

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract

class AndroidPlayerLocalStatus : PlayerContract.LocalStatus {
    override fun isPlayerActive(): Boolean = false
}
