package uk.co.sentinelweb.cuer.db.mapper

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.database.entity.Channel
import uk.co.sentinelweb.cuer.database.entity.Image
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.ChannelDomain.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.domain.ext.hasFlag

class ChannelMapper(
    private val imageMapper: ImageMapper,
    private val source: OrchestratorContract.Source,
) {

    fun map(entity: Channel, thumbEntity: Image?, imageEntity: Image?): ChannelDomain = ChannelDomain(
        id = entity.id.toGuidIdentifier(source),
        platformId = entity.platform_id,
        platform = entity.platform,
        country = entity.country,
        title = entity.title,
        customUrl = entity.custom_url,
        description = entity.description,
        published = entity.published,
        thumbNail = thumbEntity?.let { imageMapper.map(it) },
        image = imageEntity?.let { imageMapper.map(it) },
        starred = entity.flags.hasFlag(FLAG_STARRED)
    )

    fun map(domain: ChannelDomain) = Channel(
        id = domain.id?.id?.value ?: throw IllegalArgumentException("No id"),
        flags = if (domain.starred) FLAG_STARRED else 0,
        title = domain.title,
        description = domain.description,
        custom_url = domain.customUrl,
        country = domain.country,
        platform = domain.platform,
        platform_id = domain.platformId!!,
        thumb_id = domain.thumbNail?.id?.id?.value,
        image_id = domain.image?.id?.id?.value,
        published = domain.published
    )
}
