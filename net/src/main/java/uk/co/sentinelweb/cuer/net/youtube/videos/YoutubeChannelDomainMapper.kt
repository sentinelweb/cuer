package uk.co.sentinelweb.cuer.net.youtube.videos

import uk.co.sentinelweb.cuer.core.mappers.DateTimeMapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeChannelsDto

internal class YoutubeChannelDomainMapper(
    private val dateTimeMapper: DateTimeMapper
) {
    fun map(dto: YoutubeChannelsDto): List<ChannelDomain> =
        dto.items.map {
            ChannelDomain(
                id = null,
                platformId = it.id,
                title = it.snippet?.title,
                platform = PlatformDomain.YOUTUBE,
                description = it.snippet?.description,
                thumbNail = mapImage(it.snippet?.thumbnails
                    ?.let { thumbnailsDto -> thumbnailsDto.medium ?: thumbnailsDto.standard }
                ),
                image = mapImage(it.snippet?.thumbnails
                    ?.let { thumbnailsDto -> thumbnailsDto.maxres ?: thumbnailsDto.high }
                ),
                starred = false,
                published = it.snippet?.publishedAt?.let { ts -> dateTimeMapper.mapTimestamp(ts) }
            )
        }

    private fun mapImage(thumbNail: YoutubeChannelsDto.ItemDto.SnippetDto.ThumbnailsDto.ThumbnailDto?) =
        thumbNail?.let {
            ImageDomain(
                it.url,
                it.width,
                it.height
            )
        }

}