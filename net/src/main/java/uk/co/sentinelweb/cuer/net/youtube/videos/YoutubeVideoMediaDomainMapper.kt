package uk.co.sentinelweb.cuer.net.youtube.videos

import uk.co.sentinelweb.cuer.core.mappers.DateTimeMapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto

internal class YoutubeVideoMediaDomainMapper(
    private val dateTimeMapper: DateTimeMapper
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
                mediaId = it.id,
                duration = it.contentDetails?.duration
                    ?.let { dur -> dateTimeMapper.mapDuration(dur) }
                    ?: -1,
                thumbNail = mapImage(it.snippet?.thumbnails
                    ?.let { thumbnailsDto -> thumbnailsDto.medium ?: thumbnailsDto.standard }
                ),
                image = mapImage(it.snippet?.thumbnails
                    ?.let { thumbnailsDto -> thumbnailsDto.maxres ?: thumbnailsDto.high }
                ),
                channelData = ChannelDomain(
                    remoteId = it.snippet?.channelId ?: "",
                    title = it.snippet?.channelTitle
                ),
                published = it.snippet?.publishedAt?.let { ts -> dateTimeMapper.mapTimestamp(ts) }
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