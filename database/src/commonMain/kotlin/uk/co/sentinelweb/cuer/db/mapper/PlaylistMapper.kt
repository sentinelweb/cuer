package uk.co.sentinelweb.cuer.db.mapper

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.database.entity.Image
import uk.co.sentinelweb.cuer.database.entity.Playlist
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.Companion.FLAG_ARCHIVED
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.Companion.FLAG_DEFAULT
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.Companion.FLAG_PLAY_ITEMS_FROM_START
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.hasFlag
import uk.co.sentinelweb.cuer.domain.ext.makeFlags

class PlaylistMapper(
    private val imageMapper: ImageMapper,
    private val source: Source
) {

    fun map(
        entity: Playlist,
        items: List<PlaylistItemDomain>,
        channelDomain: ChannelDomain?,
        thumbEntity: Image?,
        imageEntity: Image?
    ) = PlaylistDomain(
        id = entity.id.toGuidIdentifier(source),
        title = entity.title,
        items = items,
        currentIndex = entity.currentIndex.toInt(),
        parentId = entity.parent_id?.toGuidIdentifier(source),
        mode = entity.mode,
        type = entity.type,
        platform = entity.platform,
        channelData = channelDomain,
        platformId = entity.platform_id,
        starred = entity.flags.hasFlag(FLAG_STARRED),
        archived = entity.flags.hasFlag(FLAG_ARCHIVED),
        default = entity.flags.hasFlag(FLAG_DEFAULT),
        thumb = thumbEntity?.let { imageMapper.map(it) },
        image = imageEntity?.let { imageMapper.map(it) },
        playItemsFromStart = entity.flags.hasFlag(FLAG_PLAY_ITEMS_FROM_START),
        config = entity.config_json,
    )

    fun map(domain: PlaylistDomain) = Playlist(
        id = domain.id?.id?.value ?: throw IllegalArgumentException("No id"),
        flags = mapFlags(domain),
        title = domain.title,
        currentIndex = domain.currentIndex.toLong(),
        mode = domain.mode,
        type = domain.type,
        platform = domain.platform,
        platform_id = domain.platformId,
        channel_id = domain.channelData?.id?.id?.value,
        parent_id = domain.parentId?.id?.value,
        thumb_id = domain.thumb?.id?.id?.value,
        image_id = domain.image?.id?.id?.value,
        config_json = domain.config,
    )

    private fun mapFlags(domain: PlaylistDomain): Long =
        makeFlags(
            FLAG_STARRED to domain.starred,
            FLAG_ARCHIVED to domain.archived,
            FLAG_DEFAULT to domain.default,
            FLAG_PLAY_ITEMS_FROM_START to domain.playItemsFromStart
        )
}