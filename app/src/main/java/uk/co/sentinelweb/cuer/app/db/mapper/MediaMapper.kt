package uk.co.sentinelweb.cuer.app.db.mapper

import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDateTime
import uk.co.sentinelweb.cuer.app.db.AppDatabase.Companion.INITIAL_ID
import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity
import uk.co.sentinelweb.cuer.app.db.entity.MediaAndChannel
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_LIVE
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_LIVE_UPCOMING
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_PLAY_FROM_START
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED

class MediaMapper(
    private val imageMapper: ImageMapper,
    private val channelMapper: ChannelMapper
) {
    fun map(domain: MediaDomain): MediaEntity = MediaEntity(
        id = domain.id ?: INITIAL_ID,
        url = domain.url,
        mediaId = domain.platformId,
        mediaType = domain.mediaType,
        title = domain.title,
        duration = domain.duration,
        positon = domain.positon,
        dateLastPlayed = domain.dateLastPlayed?.toJavaInstant(),
        description = domain.description,
        platform = domain.platform,
        thumbNail = imageMapper.mapImage(domain.thumbNail),
        image = imageMapper.mapImage(domain.image),
        channelId = domain.channelData.id ?: throw IllegalStateException("Channel is not saved"),
        published = domain.published?.toJavaLocalDateTime(),
        flags = (if (domain.watched) FLAG_WATCHED else 0) +
                (if (domain.starred) FLAG_STARRED else 0) +
                (if (domain.isLiveBroadcast) FLAG_LIVE else 0) +
                (if (domain.isLiveBroadcastUpcoming) FLAG_LIVE_UPCOMING else 0) +
                (if (domain.playFromStart) FLAG_PLAY_FROM_START else 0)
    )

    fun map(entity: MediaEntity, channelEntity: ChannelEntity): MediaDomain = MediaDomain(
        id = entity.id,
        url = entity.url,
        platformId = entity.mediaId,
        mediaType = entity.mediaType,
        title = entity.title,
        duration = entity.duration,
        positon = entity.positon,
        dateLastPlayed = entity.dateLastPlayed?.toKotlinInstant(),
        description = entity.description,
        platform = entity.platform,
        thumbNail = imageMapper.mapImage(entity.thumbNail),
        image = imageMapper.mapImage(entity.image),
        channelData = channelMapper.map(channelEntity),
        published = entity.published?.toKotlinLocalDateTime(),
        watched = entity.flags and FLAG_WATCHED == FLAG_WATCHED,
        starred = entity.flags and FLAG_STARRED == FLAG_STARRED,
        isLiveBroadcast = entity.flags and FLAG_LIVE == FLAG_LIVE,
        isLiveBroadcastUpcoming = entity.flags and FLAG_LIVE_UPCOMING == FLAG_LIVE_UPCOMING,
        playFromStart = entity.flags and FLAG_PLAY_FROM_START == FLAG_PLAY_FROM_START
    )

    fun map(entity: MediaAndChannel): MediaDomain = map(entity.media, entity.channel)

}