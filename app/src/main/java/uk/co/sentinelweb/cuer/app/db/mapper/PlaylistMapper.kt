package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.AppDatabase.Companion.INITIAL_ID
import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity
import uk.co.sentinelweb.cuer.app.db.entity.MediaAndChannel
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity.Companion.FLAG_ARCHIVED
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity.Companion.FLAG_DEFAULT
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistItemEntity
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistMapper(
    private val imageMapper: ImageMapper,
    private val playlistItemMapper: PlaylistItemMapper,
    private val channelMapper: ChannelMapper,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    fun map(domain: PlaylistDomain): PlaylistEntity = PlaylistEntity(
        id = domain.id ?: INITIAL_ID,
        currentIndex = domain.currentIndex, // todo enforce consistency better
        config = domain.config,
        flags = if (domain.archived) FLAG_ARCHIVED else 0 +
                if (domain.starred) FLAG_STARRED else 0 +
                        if (domain.default) FLAG_DEFAULT else 0,
        image = imageMapper.mapImage(domain.image),
        thumb = imageMapper.mapImage(domain.thumb),
        mode = domain.mode,
        title = domain.title,
        parentId = domain.parentId ?: -1,
        type = domain.type,
        platform = domain.platform,
        platformId = domain.platformId,
        channelId = domain.channelData?.id
    )

    fun mapWithMediaEntities(
        entity: PlaylistEntity,
        items: List<PlaylistItemEntity>?,
        medias: Map<Long, MediaAndChannel>?,
        channelEntity: ChannelEntity?
    ): PlaylistDomain = PlaylistDomain(
        id = entity.id,
        archived = entity.flags and FLAG_ARCHIVED == FLAG_ARCHIVED,
        starred = entity.flags and FLAG_STARRED == FLAG_STARRED,
        default = entity.flags and FLAG_DEFAULT == FLAG_DEFAULT,
        items = items
            ?.mapNotNull { item ->
                medias?.get(item.mediaId)
                    ?.let { mediaAndChannel -> playlistItemMapper.map(item, mediaAndChannel) }
                    ?: let { log.e("No media for ${item.mediaId}"); null } // throw Exception("no media found for ${item.mediaId}") // todo possibly should have a flag here db inconsistent .. but likely legacy data
            }
            ?.sortedBy { it.order }
            ?: listOf(),
        mode = entity.mode,
        thumb = imageMapper.mapImage(entity.thumb),
        image = imageMapper.mapImage(entity.image),
        config = entity.config,
        currentIndex = entity.currentIndex,
        title = entity.title,
        parentId = entity.parentId.takeIf { it > -1L },
        platformId = entity.platformId,
        channelData = channelEntity?.let { channelMapper.map(it) },
        platform = entity.platform,
        type = entity.type
    )

    fun mapWithMediaDomains(
        entity: PlaylistEntity,
        items: List<PlaylistItemEntity>?,
        medias: Map<Long, MediaDomain>,
        channelEntity: ChannelEntity?
    ): PlaylistDomain = PlaylistDomain(
        id = entity.id,
        archived = entity.flags and FLAG_ARCHIVED == FLAG_ARCHIVED,
        starred = entity.flags and FLAG_STARRED == FLAG_STARRED,
        default = entity.flags and FLAG_DEFAULT == FLAG_DEFAULT,
        items = items
            ?.mapNotNull { item ->
                medias.get(item.mediaId)
                    ?.let { mediaDomain -> playlistItemMapper.map(item, mediaDomain) }
                    ?: let { log.e("No media for ${item.mediaId}"); null }//throw Exception("no media found for ${item.mediaId}")//
            }
            ?.sortedBy { it.order }
            ?: listOf(),
        mode = entity.mode,
        thumb = imageMapper.mapImage(entity.thumb),
        image = imageMapper.mapImage(entity.image),
        config = entity.config,
        currentIndex = entity.currentIndex,
        title = entity.title,
        parentId = entity.parentId.takeIf { it > -1L },
        platformId = entity.platformId,
        channelData = channelEntity?.let { channelMapper.map(it) },
        platform = entity.platform,
        type = entity.type
    )
}