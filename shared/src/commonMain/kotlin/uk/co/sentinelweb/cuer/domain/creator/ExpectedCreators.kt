package uk.co.sentinelweb.cuer.domain.creator

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

expect class PlaylistItemCreator {
    fun buildPlayListItem(savedMedia: MediaDomain, playlist: PlaylistDomain?, order: Long? = null): PlaylistItemDomain
}