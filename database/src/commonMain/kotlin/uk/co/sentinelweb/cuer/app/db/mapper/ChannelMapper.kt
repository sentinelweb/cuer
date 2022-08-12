package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.Channel
import uk.co.sentinelweb.cuer.app.db.Image
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.ChannelDomain.Companion.FLAG_STARRED

class ChannelMapper(private val imageMapper: ImageMapper) {

    fun map(entity: Channel, thumbEntity: Image?, imageEntity: Image?): ChannelDomain = ChannelDomain(
        entity.id,
        entity.platform_id,
        entity.platform,
        entity.country,
        entity.title,
        entity.custom_url,
        entity.description,
        entity.published,
        thumbEntity?.let { imageMapper.map(it) },
        imageEntity?.let { imageMapper.map(it) }
    )

    fun map(domain: ChannelDomain) = Channel(
        domain.id ?: 0,
        if (domain.starred) FLAG_STARRED else 0,
        domain.title,
        domain.description,
        domain.customUrl,
        domain.country,
        domain.platform,
        domain.platformId!!,
        domain.image?.id,
        domain.thumbNail?.id,
        domain.published
    )
}
