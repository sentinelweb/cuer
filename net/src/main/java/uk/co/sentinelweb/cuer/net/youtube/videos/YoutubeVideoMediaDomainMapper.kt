package uk.co.sentinelweb.cuer.net.youtube.videos

import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto

internal class YoutubeVideoMediaDomainMapper(
    private val timeStampMapper: TimeStampMapper
) {
    fun map(dto: YoutubeVideosDto): List<MediaDomain> =
        dto.items.map {
            MediaDomain(
                id = null,
                url = "https://youtu.be/${it.id}",
                title = it.snippet?.title,
                description = it.snippet?.description,
                mediaType = MediaDomain.MediaTypeDomain.VIDEO,
                platform = PlatformDomain.YOUTUBE,
                platformId = it.id,
                duration = it.contentDetails?.duration
                    ?.let { dur -> timeStampMapper.mapDuration(dur) }
                    ?: -1,
                thumbNail = mapImage(it.snippet?.thumbnails
                    ?.let { thumbnailsDto -> thumbnailsDto.medium ?: thumbnailsDto.standard }
                ),
                image = mapImage(it.snippet?.thumbnails
                    ?.let { thumbnailsDto -> thumbnailsDto.maxres ?: thumbnailsDto.high }
                ),
                channelData = ChannelDomain( // todo fix
                    platformId = it.snippet?.channelId ?: "",
                    title = it.snippet?.channelTitle,
                    platform = PlatformDomain.YOUTUBE
                ),
                published = it.snippet?.publishedAt?.let { ts -> timeStampMapper.mapTimestamp(ts) },
                isLiveBroadcast = it.snippet?.liveBroadcastContent?.let { it == "live" || it == "upcoming" } ?: false,
                isLiveBroadcastUpcoming = it.snippet?.liveBroadcastContent?.let { it == "upcoming" } ?: false
            )
        }

    private fun mapImage(thumbNail: YoutubeVideosDto.VideoDto.SnippetDto.ThumbnailsDto.ThumbnailDto?) =
        thumbNail?.let {
            ImageDomain(
                it.url,
                it.width,
                it.height
            )
        }

}