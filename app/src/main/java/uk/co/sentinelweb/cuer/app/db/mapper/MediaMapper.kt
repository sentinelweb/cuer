package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
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
        platform = domain.platform
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
        platform = entity.platform
    )
}