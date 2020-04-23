package uk.co.sentinelweb.cuer.net.youtube.videos

import uk.co.sentinelweb.cuer.domain.MediaDomain
import java.time.Duration

class YoutubeVideoMediaDomainMapper {
    fun map(dto:YoutubeVideosDto):List<MediaDomain> =
        dto.items.map {
            MediaDomain(
                id = null,
                url = "https://youtu.be/${it.id}",
                title = it.snippet?.title,
                description = it.snippet?.description,
                mediaType = MediaDomain.MediaTypeDomain.VIDEO,
                platform = MediaDomain.PlatformDomain.YOUTUBE,
                mediaId = it.id,
                duration = parseDuration(it.contentDetails?.duration)
            )
        }

    private fun parseDuration(duration: String?): Long =
        duration?.let {
            Duration.parse(duration).toMillis()
        } ?: -1

}