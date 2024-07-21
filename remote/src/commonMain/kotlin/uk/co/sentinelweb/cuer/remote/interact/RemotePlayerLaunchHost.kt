package uk.co.sentinelweb.cuer.remote.interact

import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface RemotePlayerLaunchHost {

    fun launchVideo(item: PlaylistItemDomain, screenIndex: Int?)
}