package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.Image
import uk.co.sentinelweb.cuer.app.db.Media
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_LIVE
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_LIVE_UPCOMING
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_PLAY_FROM_START
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
import uk.co.sentinelweb.cuer.domain.ext.hasFlag

class MediaMapper(
    private val imageMapper: ImageMapper
) {
    fun map(entity: Media, channelDomain: ChannelDomain, thumbEntity: Image?, imageEntity: Image?): MediaDomain =
        MediaDomain(
            id = entity.id,
            url = entity.url,
            platformId = entity.platform_id,
            mediaType = entity.type,
            platform = entity.platform,
            title = entity.title,
            duration = entity.duration,
            positon = entity.position,
            dateLastPlayed = entity.date_last_played,
            description = entity.description,
            published = entity.published,
            channelData = channelDomain,
            thumbNail = thumbEntity?.let { imageMapper.map(it) },
            image = imageEntity?.let { imageMapper.map(it) },
            watched = entity.flags.hasFlag(FLAG_WATCHED),
            starred = entity.flags.hasFlag(FLAG_STARRED),
            isLiveBroadcast = entity.flags.hasFlag(FLAG_LIVE),
            isLiveBroadcastUpcoming = entity.flags.hasFlag(FLAG_LIVE_UPCOMING),
            playFromStart = entity.flags.hasFlag(FLAG_PLAY_FROM_START)
        )

    fun map(domain: MediaDomain): Media = Media(
        id = domain.id ?: 0,
        url = domain.url,
        platform_id = domain.platformId,
        type = domain.mediaType,
        platform = domain.platform,
        title = domain.title,
        duration = domain.duration,
        position = domain.positon,
        date_last_played = domain.dateLastPlayed,
        description = domain.description,
        published = domain.published,
        channel_id = domain.channelData.id,
        thumb_id = domain.thumbNail?.id,
        image_id = domain.image?.id,
        flags = mapFlags(domain)
    )

    private fun mapFlags(domain: MediaDomain):Long =
        (if (domain.watched) FLAG_WATCHED else 0) +
            (if (domain.starred) FLAG_STARRED else 0) +
            (if (domain.isLiveBroadcast) FLAG_LIVE else 0) +
            (if (domain.isLiveBroadcastUpcoming) FLAG_LIVE_UPCOMING else 0) +
            (if (domain.playFromStart) FLAG_PLAY_FROM_START else 0)
}