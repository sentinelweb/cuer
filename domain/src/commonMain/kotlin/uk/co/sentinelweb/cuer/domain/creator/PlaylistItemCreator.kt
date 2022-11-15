package uk.co.sentinelweb.cuer.domain.creator

import kotlinx.datetime.Instant
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistItemCreator constructor(
    private val timeProvider: TimeProvider
) {
    fun buildPlayListItem(media: MediaDomain, playlist: PlaylistDomain?, order: Long? = null, dateAdded: Instant) =
        PlaylistItemDomain(
            media = media,
            dateAdded = dateAdded,
            playlistId = playlist?.id,
            order = order ?: timeProvider.currentTimeMillis(), // fixme this could give duplicate ordering numbers
            archived = false
        )
}