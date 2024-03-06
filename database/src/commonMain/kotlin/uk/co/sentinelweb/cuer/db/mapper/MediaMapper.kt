package uk.co.sentinelweb.cuer.db.mapper

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.database.entity.Image
import uk.co.sentinelweb.cuer.database.entity.Media
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_LIVE
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_LIVE_UPCOMING
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_PLAY_FROM_START
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
import uk.co.sentinelweb.cuer.domain.ext.hasFlag
import uk.co.sentinelweb.cuer.domain.ext.makeFlags

class MediaMapper(
    private val imageMapper: ImageMapper,
    private val source: Source
) {
    fun map(entity: Media, channelDomain: ChannelDomain, thumbEntity: Image?, imageEntity: Image?): MediaDomain =
        MediaDomain(
            id = entity.id.toGuidIdentifier(source),
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
            playFromStart = entity.flags.hasFlag(FLAG_PLAY_FROM_START),
            broadcastDate = entity.broadcast_date
        )

    fun map(domain: MediaDomain): Media = Media(
        id = domain.id?.id?.value ?: throw IllegalArgumentException("No id"),
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
        channel_id = domain.channelData.id!!.id.value,
        thumb_id = domain.thumbNail?.id?.id?.value,
        image_id = domain.image?.id?.id?.value,
        flags = mapFlags(domain),
        broadcast_date = domain.broadcastDate
    )

    private fun mapFlags(domain: MediaDomain):Long =
        makeFlags(
            FLAG_WATCHED to domain.watched,
            FLAG_STARRED to domain.starred,
            FLAG_LIVE to domain.isLiveBroadcast,
            FLAG_LIVE_UPCOMING to domain.isLiveBroadcastUpcoming,
            FLAG_PLAY_FROM_START to domain.playFromStart
        )
}