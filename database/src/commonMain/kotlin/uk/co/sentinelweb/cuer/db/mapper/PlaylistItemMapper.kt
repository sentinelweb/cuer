package uk.co.sentinelweb.cuer.db.mapper

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.database.entity.Playlist_item
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain.Companion.FLAG_ARCHIVED
import uk.co.sentinelweb.cuer.domain.ext.hasFlag

class PlaylistItemMapper(private val source: Source) {
    fun map(entity: Playlist_item, mediaDomain: MediaDomain) = PlaylistItemDomain(
        id = entity.id.toGuidIdentifier(source),
        media = mediaDomain,
        dateAdded = entity.date_added,
        order = entity.ordering,
        archived = entity.flags.hasFlag(FLAG_ARCHIVED),
        playlistId = entity.playlist_id.toGuidIdentifier(source)
    )

    fun map(domain: PlaylistItemDomain) = Playlist_item(
        id = domain.id?.id?.value ?: throw IllegalArgumentException("No id"),
        flags = mapFlags(domain),
        media_id = domain.media.id?.id?.value ?: throw IllegalArgumentException("No media id"),
        ordering = domain.order,
        playlist_id = domain.playlistId?.id?.value ?: throw IllegalArgumentException("No playlist id"),
        date_added = domain.dateAdded
    )

    private fun mapFlags(domain: PlaylistItemDomain): Long =
        (if (domain.archived) FLAG_ARCHIVED else 0)
}