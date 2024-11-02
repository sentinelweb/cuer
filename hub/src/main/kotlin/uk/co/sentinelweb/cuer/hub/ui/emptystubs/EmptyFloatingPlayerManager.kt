package uk.co.sentinelweb.cuer.hub.ui.emptystubs

import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain

class EmptyFloatingPlayerManager : FloatingPlayerContract.Manager {
    override fun get(): FloatingPlayerContract.Service? = null

    override fun isRunning(): Boolean = false

    override fun playItem(item: PlaylistAndItemDomain) = Unit
}
