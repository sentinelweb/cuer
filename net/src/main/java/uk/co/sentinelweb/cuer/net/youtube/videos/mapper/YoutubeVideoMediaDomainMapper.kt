package uk.co.sentinelweb.cuer.net.youtube.videos.mapper

import kotlinx.datetime.toKotlinLocalDateTime
import uk.co.sentinelweb.cuer.net.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto.Companion.LIVE
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto.Companion.UPCOMING

internal class YoutubeVideoMediaDomainMapper(
    private val timeStampMapper: TimeStampMapper,
    private val imageMapper: YoutubeImageMapper
) {
    fun map(dto: YoutubeVideosDto): List<MediaDomain> =
        dto.items.map {
            MediaDomain(
                id = null,
                url = "https://youtu.be/${it.id}",
                title = it.snippet.title,
                description = it.snippet.description,
                mediaType = MediaDomain.MediaTypeDomain.VIDEO,
                platform = PlatformDomain.YOUTUBE,
                platformId = it.id,
                duration = it.contentDetails?.duration
                    ?.let { dur -> timeStampMapper.mapDuration(dur) }
                    ?: -1,
                thumbNail = imageMapper.mapThumb(it.snippet.thumbnails),
                image = imageMapper.mapImage(it.snippet.thumbnails),
                channelData = ChannelDomain( // todo fix
                    platformId = it.snippet.channelId,
                    title = it.snippet.channelTitle,
                    platform = PlatformDomain.YOUTUBE
                ),
                published = it.snippet.publishedAt.let { ts -> timeStampMapper.mapTimestamp(ts)?.toKotlinLocalDateTime() },
                isLiveBroadcast = it.snippet.liveBroadcastContent.let { it == LIVE || it == UPCOMING },
                isLiveBroadcastUpcoming = it.snippet.liveBroadcastContent.let { it == UPCOMING }
            )
        }

}