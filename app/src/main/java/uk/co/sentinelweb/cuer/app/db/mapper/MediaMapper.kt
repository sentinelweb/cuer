package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.AppDatabase.Companion.INITIAL_ID
import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity
import uk.co.sentinelweb.cuer.app.db.entity.MediaAndChannel
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity.Companion.FLAG_WATCHED
import uk.co.sentinelweb.cuer.domain.MediaDomain

class MediaMapper(
    private val imageMapper: ImageMapper,
    private val channelMapper: ChannelMapper
) {
    fun map(domain: MediaDomain): MediaEntity = MediaEntity(
        id = domain.id?.toLong() ?: INITIAL_ID,
        url = domain.url,
        mediaId = domain.remoteId,
        mediaType = domain.mediaType,
        title = domain.title,
        duration = domain.duration,
        positon = domain.positon,
        dateLastPlayed = domain.dateLastPlayed,
        description = domain.description,
        platform = domain.platform,
        thumbNail = imageMapper.mapImage(domain.thumbNail),
        image = imageMapper.mapImage(domain.image),
        channelId = domain.channelData.id!!.toLong(),
        published = domain.published,
        flags = if (domain.watched) FLAG_WATCHED else 0 +
                if (domain.starred) FLAG_STARRED else 0
    )

    fun map(entity: MediaEntity, channelEntity: ChannelEntity): MediaDomain = MediaDomain(
        id = entity.id.toString(),
        url = entity.url,
        remoteId = entity.mediaId,
        mediaType = entity.mediaType,
        title = entity.title,
        duration = entity.duration,
        positon = entity.positon,
        dateLastPlayed = entity.dateLastPlayed,
        description = entity.description,
        platform = entity.platform,
        thumbNail = imageMapper.mapImage(entity.thumbNail),
        image = imageMapper.mapImage(entity.image),
        channelData = channelMapper.map(channelEntity),
        published = entity.published,
        watched = entity.flags and FLAG_WATCHED == FLAG_WATCHED,
        starred = entity.flags and FLAG_STARRED == FLAG_STARRED
    )

    fun map(entity: MediaAndChannel): MediaDomain = map(entity.media, entity.channel)

}