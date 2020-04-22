package uk.co.sentinelweb.cuer.app.queue

import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import java.time.Instant

class MediaToPlaylistItemMapper {
    fun map(media: MediaDomain): PlaylistItemDomain = PlaylistItemDomain(
        media,
        Instant.now(),
        0
    )
}