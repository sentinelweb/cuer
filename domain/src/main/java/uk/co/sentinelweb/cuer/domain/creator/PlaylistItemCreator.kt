package uk.co.sentinelweb.cuer.domain.creator

import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistItemCreator constructor(
    val timeProvider: TimeProvider
) {
    fun buildPlayListItem(savedMedia: MediaDomain, playlist: PlaylistDomain) = PlaylistItemDomain(
        media = savedMedia,
        dateAdded = timeProvider.instant(),
        playlistId = playlist.id,
        order = timeProvider.currentTimeMillis(),
        archived = false
    )
}