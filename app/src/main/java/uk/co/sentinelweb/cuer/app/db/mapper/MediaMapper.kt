package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain

class MediaMapper() {
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
        thumbNail = mapImage(domain.thumbNail),
        image = mapImage(domain.image),
        channelTitle = domain.channelTitle,
        channelId = domain.channelId,
        published = domain.published
    )

    fun map(entity: MediaEntity): MediaDomain = MediaDomain(
        id = entity.id.toString(),
        url = entity.url,
        mediaId = entity.mediaId,
        mediaType = entity.mediaType,
        title = entity.title,
        duration = entity.duration,
        positon = entity.positon,
        dateLastPlayed = entity.dateLastPlayed,
        description = entity.description,
        platform = entity.platform,
        thumbNail = mapImage(entity.thumbNail),
        image = mapImage(entity.image),
        channelTitle = entity.channelTitle,
        channelId = entity.channelId,
        published = entity.published

    )

    private fun mapImage(thumbNail: ImageDomain?) =
        thumbNail?.let {
            MediaEntity.Image(
                it.url,
                it.width,
                it.height
            )
        }

    private fun mapImage(thumbNail: MediaEntity.Image?) =
        thumbNail?.let {
            ImageDomain(
                it.url,
                it.width,
                it.height
            )
        }
}