package uk.co.sentinelweb.cuer.remote.interact

import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface PlayerLaunchHost {

    fun launchVideo(item: PlaylistItemDomain, screenIndex: Int?)
}
