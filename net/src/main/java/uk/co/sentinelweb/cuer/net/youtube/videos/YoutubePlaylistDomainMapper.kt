package uk.co.sentinelweb.cuer.net.youtube.videos

import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubePlaylistDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubePlaylistItemDto

internal class YoutubePlaylistDomainMapper(
    private val timeStampMapper: TimeStampMapper,
    private val itemCreator: PlaylistItemCreator
) {
    fun map(dto: YoutubePlaylistDto, items: List<YoutubePlaylistItemDto.PlaylistItemDto>): List<PlaylistDomain> =
        dto.items.map {
            PlaylistDomain(
                id = null,
                title = it.snippet.title,
                platform = PlatformDomain.YOUTUBE,
                type = PlaylistDomain.PlaylistTypeDomain.PLATFORM,
                platformId = it.id,
                thumb = mapImage(it.snippet.thumbnails
                    .let { thumbnailsDto -> thumbnailsDto.medium ?: thumbnailsDto.standard }
                ),
                image = mapImage(it.snippet.thumbnails
                    .let { thumbnailsDto -> thumbnailsDto.maxres ?: thumbnailsDto.high }
                ),
                channelData = ChannelDomain( // todo fix
                    platformId = it.snippet.channelId,
                    title = it.snippet.channelTitle,
                    platform = PlatformDomain.YOUTUBE
                ),
                config = PlaylistDomain.PlaylistConfigDomain(
                    updateUrl = "https://www.youtube.com/playlist?list=${it.id}",
                    description = it.snippet.description,
                    published = it.snippet.publishedAt?.let { ts -> timeStampMapper.mapTimestamp(ts) }
                ),
                items = mapItems(items)
            )
        }

    private fun mapItems(items: List<YoutubePlaylistItemDto.PlaylistItemDto>): List<PlaylistItemDomain> = items.map {
        itemCreator.buildPlayListItem(mapMedia(it), null)
    }

    private fun mapMedia(it: YoutubePlaylistItemDto.PlaylistItemDto) = MediaDomain(
        id = null,
        url = "https://youtu.be/${it.id}",
        title = it.snippet?.title,
        description = it.snippet?.description,
        mediaType = MediaDomain.MediaTypeDomain.VIDEO,
        platform = PlatformDomain.YOUTUBE,
        platformId = it.id,
        duration = 0,
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
        isLiveBroadcast = false,
        isLiveBroadcastUpcoming = false
    )

    private fun mapImage(thumbNail: YoutubePlaylistDto.PlaylistDto.SnippetDto.ThumbnailsDto.ThumbnailDto?) =
        thumbNail?.let {
            ImageDomain(
                it.url,
                it.width,
                it.height
            )
        }

    private fun mapImage(thumbNail: YoutubePlaylistItemDto.PlaylistItemDto.SnippetDto.ThumbnailsDto.ThumbnailDto?) =
        thumbNail?.let {
            ImageDomain(
                it.url,
                it.width,
                it.height
            )
        }

}