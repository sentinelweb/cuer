package uk.co.sentinelweb.cuer.app.queue

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import java.time.Instant

class MediaToPlaylistItemMapper {

    fun mapToPlaylistItem(media: MediaDomain): PlaylistItemDomain = PlaylistItemDomain(
        media = media,
        dateAdded = Instant.now(),// todo
        order = 0 // todo
    )
}