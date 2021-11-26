package uk.co.sentinelweb.cuer.domain.creator

import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistItemCreator constructor(
    private val timeProvider: TimeProvider
) {
    fun buildPlayListItem(savedMedia: MediaDomain, playlist: PlaylistDomain?, order: Long? = null) =
        PlaylistItemDomain(
            media = savedMedia,
            dateAdded = timeProvider.instant(),
            playlistId = playlist?.id,
            order = order ?: timeProvider.currentTimeMillis(),
            archived = false
        )
}