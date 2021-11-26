package uk.co.sentinelweb.cuer.app.db.mapper

import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import uk.co.sentinelweb.cuer.app.db.AppDatabase.Companion.INITIAL_ID
import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity
import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.domain.ChannelDomain

class ChannelMapper constructor(val imageMapper: ImageMapper) {
    fun map(domain: ChannelDomain): ChannelEntity = ChannelEntity(
        id = domain.id ?: INITIAL_ID,
        remoteId = domain.platformId!!,
        title = domain.title,
        description = domain.description,
        customUrl = domain.customUrl,
        country = domain.country,
        platform = domain.platform,
        thumbNail = imageMapper.mapImage(domain.thumbNail),
        image = imageMapper.mapImage(domain.image),
        flags = if (domain.starred) FLAG_STARRED else 0,
        published = domain.published?.toJavaLocalDateTime()
    )

    fun map(entity: ChannelEntity): ChannelDomain = ChannelDomain(
        id = entity.id,
        platformId = entity.remoteId,
        title = entity.title,
        description = entity.description,
        customUrl = entity.customUrl,
        country = entity.country,
        platform = entity.platform,
        thumbNail = imageMapper.mapImage(entity.thumbNail),
        image = imageMapper.mapImage(entity.image),
        starred = entity.flags and FLAG_STARRED == FLAG_STARRED,
        published = entity.published?.toKotlinLocalDateTime()
    )
}