package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity
import uk.co.sentinelweb.cuer.domain.ChannelDomain

class ChannelMapper constructor(val imageMapper: ImageMapper) {
    fun map(domain: ChannelDomain): ChannelEntity = ChannelEntity(
        id = domain.id.toInt(),
        remoteId = domain.remoteId,
        title = domain.title,
        description = domain.description,
        customUrl = domain.customUrl,
        country = domain.country,
        platform = domain.platform,
        thumbNail = imageMapper.mapImage(domain.thumbNail),
        image = imageMapper.mapImage(domain.image),
        flags = if (domain.starred) 1 else 0,
        published = domain.published
    )

    fun map(entity: ChannelEntity): ChannelDomain = ChannelDomain(
        id = entity.id.toString(),
        remoteId = entity.remoteId,
        title = entity.title,
        description = entity.description,
        customUrl = entity.customUrl,
        country = entity.country,
        platform = entity.platform,
        thumbNail = imageMapper.mapImage(entity.thumbNail),
        image = imageMapper.mapImage(entity.image),
        starred = entity.flags and 1 == 1L,
        published = entity.published
    )
}