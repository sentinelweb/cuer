package uk.co.sentinelweb.cuer.net.youtube.videos.mapper

import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.ThumbnailDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.ThumbnailsDto

internal class YoutubeImageMapper {
    fun mapThumb(thumbnails: ThumbnailsDto) =
        map(thumbnails
            .let { thumbnailsDto -> thumbnailsDto.medium ?: thumbnailsDto.default })

    fun mapImage(thumbnails: ThumbnailsDto) =
        map(thumbnails
            .let { thumbnailsDto -> thumbnailsDto.maxres ?: thumbnailsDto.standard })


    private fun map(thumbNail: ThumbnailDto?) =
        thumbNail?.let {
            ImageDomain(
                it.url,
                it.width,
                it.height
            )
        }
}