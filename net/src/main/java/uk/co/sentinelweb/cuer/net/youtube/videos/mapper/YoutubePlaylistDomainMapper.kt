package uk.co.sentinelweb.cuer.net.youtube.videos.mapper

import kotlinx.datetime.toKotlinLocalDateTime
import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.net.exception.BadDataException
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeChannelsDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubePlaylistDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubePlaylistItemDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto

internal class YoutubePlaylistDomainMapper(
    private val timeStampMapper: TimeStampMapper,
    private val itemCreator: PlaylistItemCreator,
    private val imageMapper: YoutubeImageMapper,
    private val channelMapper: YoutubeChannelDomainMapper
) {
    fun map(
        dto: YoutubePlaylistDto,
        items: List<YoutubePlaylistItemDto.PlaylistItemDto>,
        videoDtos: List<YoutubeVideosDto.VideoDto>,
        channelDtos: List<YoutubeChannelsDto.ItemDto>
    ): PlaylistDomain {
        val videoLookup = videoDtos.associateBy({ it.id }, { it })
        val channelLookup = channelDtos.associateBy({ it.id }, { channelMapper.map(it) })
        return dto.items[0].let {
            PlaylistDomain(
                id = null,
                title = it.snippet.title,
                platform = PlatformDomain.YOUTUBE,
                type = PlaylistDomain.PlaylistTypeDomain.PLATFORM,
                platformId = it.id,
                thumb = imageMapper.mapThumb(it.snippet.thumbnails),
                image = imageMapper.mapImage(it.snippet.thumbnails),
                channelData = channelLookup[it.snippet.channelId] ?: throw IllegalStateException("Channel data not found"),
                config = PlaylistDomain.PlaylistConfigDomain(
                    platformUrl = "https://youtube.com/playlist?list=${it.id}",
                    description = it.snippet.description,
                    published = it.snippet.publishedAt.let { ts -> timeStampMapper.mapTimestamp(ts)?.toKotlinLocalDateTime() }
                ),
                items = mapItems(items, videoLookup, channelLookup)
            )
        }
    }

    private fun mapItems(
        items: List<YoutubePlaylistItemDto.PlaylistItemDto>,
        videoLookup: Map<String, YoutubeVideosDto.VideoDto>,
        channelLookup: Map<String, ChannelDomain>
    ): List<PlaylistItemDomain> = items
        .filter { videoLookup.keys.contains<String>(it.snippet.resourceId.videoId) } // some videos are deleted
        .map {
            itemCreator.buildPlayListItem(mapMedia(it, videoLookup, channelLookup), null, order = it.snippet.position * 1000L)
        }

    private fun mapMedia(
        item: YoutubePlaylistItemDto.PlaylistItemDto,
        videoLookup: Map<String, YoutubeVideosDto.VideoDto>,
        channelLookup: Map<String, ChannelDomain>
    ) = item.snippet.let {
        MediaDomain(
            id = null,
            url = "https://youtu.be/${it.resourceId.videoId}",
            title = it.title,
            description = it.description,
            mediaType = MediaDomain.MediaTypeDomain.VIDEO,
            platform = PlatformDomain.YOUTUBE,
            platformId = it.resourceId.videoId,
            duration = videoLookup[it.resourceId.videoId]?.contentDetails?.duration
                ?.let { dur -> timeStampMapper.mapDuration(dur) }
                ?: -1,
            thumbNail = imageMapper.mapThumb(it.thumbnails),
            image = imageMapper.mapImage(it.thumbnails),
            channelData = it.videoOwnerChannelId?.let { channelLookup[it] }
                ?: throw BadDataException("Channel not found: ownerId:${it.videoOwnerChannelId} videoId:${it.resourceId.videoId}"),
            published = it.publishedAt.let { ts -> timeStampMapper.mapTimestamp(ts)?.toKotlinLocalDateTime() },
            isLiveBroadcast = videoLookup[it.resourceId.videoId]
                ?.snippet?.liveBroadcastContent
                ?.let { it == YoutubeVideosDto.LIVE || it == YoutubeVideosDto.UPCOMING }
                ?: false,
            isLiveBroadcastUpcoming = videoLookup[it.resourceId.videoId]
                ?.snippet?.liveBroadcastContent
                ?.let { it == YoutubeVideosDto.UPCOMING }
                ?: false
        )
    }

}