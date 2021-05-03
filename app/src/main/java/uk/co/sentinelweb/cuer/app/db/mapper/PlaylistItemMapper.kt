package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.entity.*
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistItemEntity.Companion.FLAG_ARCHIVED
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistItemMapper(
    private val mediaMapper: MediaMapper
) {
    fun map(domain: PlaylistItemDomain): PlaylistItemEntity = PlaylistItemEntity(
        id = domain.id ?: AppDatabase.INITIAL_ID,
        mediaId = domain.media.id!!.toLong(),// todo enforce consistency better
        order = domain.order,
        flags = if (domain.archived) FLAG_ARCHIVED else 0,
        playlistId = domain.playlistId
            ?: throw IllegalStateException("playlist item has no laylist id"),
        dateAdded = domain.dateAdded
    )

    fun map(domain: MediaDomain): MediaEntity = mediaMapper.map(domain)

    fun map(entity: PlaylistItemEntity, media: MediaAndChannel): PlaylistItemDomain =
        map(entity, media.media, media.channel)

    fun map(
        entity: PlaylistItemEntity,
        mediaEntity: MediaEntity,
        channelEntity: ChannelEntity
    ): PlaylistItemDomain = PlaylistItemDomain(
        id = entity.id,
        media = mediaMapper.map(mediaEntity, channelEntity),// todo enforce consistency better
        order = entity.order,
        archived = entity.flags and FLAG_ARCHIVED == FLAG_ARCHIVED,
        dateAdded = entity.dateAdded,
        playlistId = entity.playlistId
    )

    fun map(
        entity: PlaylistItemEntity,
        mediaDomain: MediaDomain
    ): PlaylistItemDomain = PlaylistItemDomain(
        id = entity.id,
        media = if (entity.mediaId == mediaDomain.id) mediaDomain else throw IllegalStateException("Media id does not match item"),
        order = entity.order,
        archived = entity.flags and FLAG_ARCHIVED == FLAG_ARCHIVED,
        dateAdded = entity.dateAdded,
        playlistId = entity.playlistId
    )

    fun map(
        entity: PlaylistItemAndMediaAndChannel
    ): PlaylistItemDomain = PlaylistItemDomain(
        id = entity.item.id,
        media = mediaMapper.map(entity.media.media, entity.media.channel),
        order = entity.item.order,
        archived = entity.item.flags and FLAG_ARCHIVED == FLAG_ARCHIVED,
        dateAdded = entity.item.dateAdded,
        playlistId = entity.item.playlistId
    )

}