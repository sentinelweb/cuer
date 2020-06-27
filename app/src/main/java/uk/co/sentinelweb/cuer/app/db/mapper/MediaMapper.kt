package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.entity.MediaAndChannel
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.domain.MediaDomain

class MediaMapper(
    private val imageMapper: ImageMapper,
    private val channelMapper: ChannelMapper

) {
    fun map(domain: MediaDomain): MediaEntity = MediaEntity(
        id = domain.id?.toInt() ?: 0,
        url = domain.url,
        mediaId = domain.mediaId,
        mediaType = domain.mediaType,
        title = domain.title,
        duration = domain.duration,
        positon = domain.positon,
        dateLastPlayed = domain.dateLastPlayed,
        description = domain.description,
        platform = domain.platform,
        thumbNail = imageMapper.mapImage(domain.thumbNail),
        image = imageMapper.mapImage(domain.image),
        channelId = domain.channelData.id.toInt(),
        published = domain.published
    )

    fun map(entity: MediaAndChannel): MediaDomain = MediaDomain(
        id = entity.media.id.toString(),
        url = entity.media.url,
        mediaId = entity.media.mediaId,
        mediaType = entity.media.mediaType,
        title = entity.media.title,
        duration = entity.media.duration,
        positon = entity.media.positon,
        dateLastPlayed = entity.media.dateLastPlayed,
        description = entity.media.description,
        platform = entity.media.platform,
        thumbNail = imageMapper.mapImage(entity.media.thumbNail),
        image = imageMapper.mapImage(entity.media.image),
        channelData = channelMapper.map(entity.channel),
        published = entity.media.published
    )


}