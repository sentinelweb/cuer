package uk.co.sentinelweb.cuer.net.youtube.videos.mapper

import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeChannelsDto

internal class YoutubeChannelDomainMapper(
    private val timeStampMapper: TimeStampMapper,
    private val imageMapper: YoutubeImageMapper
) {
    fun map(dto: YoutubeChannelsDto): List<ChannelDomain> =
        dto.items.map {
            map(it)
        }

    fun map(it: YoutubeChannelsDto.ItemDto) = ChannelDomain(
        id = null,
        platformId = it.id,
        title = it.snippet.title,
        platform = PlatformDomain.YOUTUBE,
        description = it.snippet.description,
        thumbNail = imageMapper.mapThumb(it.snippet.thumbnails),
        image = imageMapper.mapImage(it.snippet.thumbnails),
        starred = false,
        customUrl = it.snippet.customUrl?.let { "https://youtube.com/channel/$it" },
        published = it.snippet.publishedAt.let { ts -> timeStampMapper.parseTimestamp(ts) }
    )
}