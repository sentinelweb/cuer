package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.entity.*
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistItemEntity.Companion.FLAG_ARCHIVED
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistItemMapper(
    private val mediaMapper: MediaMapper
) {
    fun map(domain: PlaylistItemDomain, playlistId: Long): PlaylistItemEntity = PlaylistItemEntity(
        id = domain.id?.toLong() ?: 0,
        mediaId = domain.media.id!!.toLong(),// todo enforce consistency better
        order = domain.order,
        flags = if (domain.archived) FLAG_ARCHIVED else 0,
        playlistId = playlistId,
        dateAdded = domain.dateAdded
    )

    fun map(entity: PlaylistItemEntity, media: MediaAndChannel): PlaylistItemDomain =
        map(entity, media.media, media.channel)

    fun map(entity: PlaylistItemAndMedia, channelEntity: ChannelEntity): PlaylistItemDomain =
        map(entity.item, entity.media, channelEntity)

    fun map(
        entity: PlaylistItemEntity,
        mediaEntity: MediaEntity,
        channelEntity: ChannelEntity
    ): PlaylistItemDomain = PlaylistItemDomain(
        id = entity.id.toString(),
        media = mediaMapper.map(mediaEntity, channelEntity),// todo enforce consistency better
        order = entity.order,
        archived = entity.flags and FLAG_ARCHIVED == FLAG_ARCHIVED,
        dateAdded = entity.dateAdded
    )

}