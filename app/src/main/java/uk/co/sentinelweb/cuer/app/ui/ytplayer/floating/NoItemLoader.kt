package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract

class NoItemLoader : PlayerContract.PlaylistItemLoader {

    override fun load() = null
}