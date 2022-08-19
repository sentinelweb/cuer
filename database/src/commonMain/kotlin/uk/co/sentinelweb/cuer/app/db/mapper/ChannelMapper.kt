package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.Channel
import uk.co.sentinelweb.cuer.app.db.Image
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.ChannelDomain.Companion.FLAG_STARRED

class ChannelMapper(private val imageMapper: ImageMapper) {

    fun map(entity: Channel, thumbEntity: Image?, imageEntity: Image?): ChannelDomain = ChannelDomain(
        id = entity.id,
        platformId = entity.platform_id,
        platform = entity.platform,
        country = entity.country,
        title = entity.title,
        customUrl = entity.custom_url,
        description = entity.description,
        published = entity.published,
        thumbNail = thumbEntity?.let { imageMapper.map(it) },
        image = imageEntity?.let { imageMapper.map(it) },
        starred = (entity.flags and FLAG_STARRED) == FLAG_STARRED
    )

    fun map(domain: ChannelDomain) = Channel(
        id = domain.id ?: 0,
        flags = if (domain.starred) FLAG_STARRED else 0,
        title = domain.title,
        description = domain.description,
        custom_url = domain.customUrl,
        country = domain.country,
        platform = domain.platform,
        platform_id = domain.platformId!!,
        thumb_id = domain.thumbNail?.id,
        image_id = domain.image?.id,
        published = domain.published
    )
}
