package uk.co.sentinelweb.cuer.db.mapper

import uk.co.sentinelweb.cuer.database.entity.Playlist_item
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain.Companion.FLAG_ARCHIVED
import uk.co.sentinelweb.cuer.domain.ext.hasFlag

class PlaylistItemMapper {
    fun map(entity: Playlist_item, mediaDomain: MediaDomain) = PlaylistItemDomain(
        id = entity.id,
        media = mediaDomain,
        dateAdded = entity.date_added,
        order = entity.ordering,
        archived = entity.flags.hasFlag(FLAG_ARCHIVED),
        playlistId = entity.playlist_id
    )

    fun map(domain: PlaylistItemDomain) = Playlist_item(
        id = domain.id?:0,
        flags = mapFlags(domain),
        media_id = domain.media.id ?: throw IllegalArgumentException("No media id"),
        ordering = domain.order,
        playlist_id = domain.playlistId ?: throw IllegalArgumentException("No playlist id"),
        date_added = domain.dateAdded
    )

    private fun mapFlags(domain: PlaylistItemDomain):Long =
        (if (domain.archived) FLAG_ARCHIVED else 0)
}