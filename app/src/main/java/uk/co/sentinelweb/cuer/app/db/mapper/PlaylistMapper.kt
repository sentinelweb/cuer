package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.AppDatabase.Companion.INITIAL_ID
import uk.co.sentinelweb.cuer.app.db.entity.MediaAndChannel
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity.Companion.FLAG_ARCHIVED
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistItemEntity
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistMapper(
    private val imageMapper: ImageMapper,
    private val playlistItemMapper: PlaylistItemMapper
) {
    fun map(domain: PlaylistDomain): PlaylistEntity = PlaylistEntity(
        id = domain.id?.toLong() ?: INITIAL_ID,
        currentIndex = domain.currentIndex, // todo enforce consistency better
        config = domain.config,
        flags = if (domain.archived) FLAG_ARCHIVED else 0 +
                if (domain.starred) FLAG_STARRED else 0,
        image = imageMapper.mapImage(domain.image),
        thumb = imageMapper.mapImage(domain.thumb),
        mode = domain.mode,
        title = domain.title
    )

    fun map(
        entity: PlaylistEntity,
        items: List<PlaylistItemEntity>?,
        medias: List<MediaAndChannel>?
    ): PlaylistDomain = PlaylistDomain(
        id = entity.id.toString(),
        archived = entity.flags and FLAG_ARCHIVED == FLAG_ARCHIVED,
        starred = entity.flags and FLAG_STARRED == FLAG_STARRED,
        items = items
            ?.map {
                playlistItemMapper.map(
                    it,
                    medias!!.find { media -> media.media.id == it.mediaId }!!
                )
            }
            ?: listOf(),
        mode = entity.mode,
        thumb = imageMapper.mapImage(entity.thumb),
        image = imageMapper.mapImage(entity.image),
        config = entity.config,
        currentIndex = entity.currentIndex,
        title = entity.title
    )

}