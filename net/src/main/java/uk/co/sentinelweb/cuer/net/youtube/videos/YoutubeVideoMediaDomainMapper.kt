package uk.co.sentinelweb.cuer.net.youtube.videos

import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class YoutubeVideoMediaDomainMapper {
    fun map(dto: YoutubeVideosDto): List<MediaDomain> =
        dto.items.map {
            MediaDomain(
                id = null,
                url = "https://youtu.be/${it.id}",
                title = it.snippet?.title,
                description = it.snippet?.description,
                mediaType = MediaDomain.MediaTypeDomain.VIDEO,
                platform = MediaDomain.PlatformDomain.YOUTUBE,
                mediaId = it.id,
                duration = parseDuration(it.contentDetails?.duration),
                thumbNail = mapImage(it.snippet?.thumbnails?.let { it.medium ?: it.standard }),
                image = mapImage(it.snippet?.thumbnails?.let { it.maxres ?: it.high }),
                channelId = it.snippet?.channelId,
                channelTitle = it.snippet?.channelTitle,
                published = parseLocalDateTime(it.snippet?.publishedAt)
            )
        }

    private fun parseLocalDateTime(publishedAt: String?): LocalDateTime? =
        publishedAt?.let {
            LocalDateTime.parse(
                publishedAt,
                DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'")
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

    private fun parseDuration(duration: String?): Long =
        duration?.let {
            Duration.parse(duration).toMillis()
        } ?: -1

}