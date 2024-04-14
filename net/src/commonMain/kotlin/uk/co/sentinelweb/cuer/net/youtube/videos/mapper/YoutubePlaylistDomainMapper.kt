package uk.co.sentinelweb.cuer.net.youtube.videos.mapper

import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeChannelsDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubePlaylistDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubePlaylistItemDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto

internal class YoutubePlaylistDomainMapper(
    private val timeStampMapper: TimeStampMapper,
    private val itemCreator: PlaylistItemCreator,
    private val imageMapper: YoutubeImageMapper,
    private val channelMapper: YoutubeChannelDomainMapper,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

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
                type = PLATFORM,
                platform = PlatformDomain.YOUTUBE,
                platformId = it.id,
                thumb = imageMapper.mapThumb(it.snippet.thumbnails),
                image = imageMapper.mapImage(it.snippet.thumbnails),
                channelData = channelLookup[it.snippet.channelId]
                    ?: throw IllegalStateException("Channel data not found"),
                config = PlaylistDomain.PlaylistConfigDomain(
                    platformUrl = "https://youtube.com/playlist?list=${it.id}",
                    description = it.snippet.description,
                    published = it.snippet.publishedAt.let { ts ->
                        log.d("pl.pub: before parse: $ts")
                        timeStampMapper.parseTimestamp(ts).also { log.d("pl.pub: after parse: $it") }
                    }
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
        .mapNotNull {
            mapMedia(it, videoLookup, channelLookup)
                ?.let { mediaDomain ->
                    itemCreator.buildPlayListItem(
                        media = mediaDomain,
                        playlist = null,
                        order = it.snippet.position * 1000L,
                        dateAdded = it.snippet.publishedAt
                            .let { ts ->
                                log.d("pli.add: before parse: $ts")
                                timeStampMapper.parseTimestampInstant(ts)
                                    .also { log.d("pli.add: after parse: $it") }
                            }
                    )
                }
        }

    private fun mapMedia(
        item: YoutubePlaylistItemDto.PlaylistItemDto,
        videoLookup: Map<String, YoutubeVideosDto.VideoDto>,
        channelLookup: Map<String, ChannelDomain>
    ) = item.snippet.let {
        val videoDto = videoLookup[it.resourceId.videoId]
            ?: return@let null // throw BadDataException("No video for playlist item: ${it.resourceId.videoId}")

        val channelDomain = it.videoOwnerChannelId
            .let { channelLookup[it] }
            ?: return@let null //throw BadDataException(
//                "Channel not found: ownerId:${it.videoOwnerChannelId} videoId:${it.resourceId.videoId}"
//            )
        MediaDomain(
            id = null,
            url = "https://youtu.be/${it.resourceId.videoId}",
            title = it.title,
            description = it.description,
            mediaType = MediaDomain.MediaTypeDomain.VIDEO,
            platform = PlatformDomain.YOUTUBE,
            platformId = it.resourceId.videoId,
            duration = videoDto.contentDetails?.duration
                ?.let { dur -> timeStampMapper.parseDuration(dur) }
                ?: -1,
            thumbNail = imageMapper.mapThumb(it.thumbnails),
            image = imageMapper.mapImage(it.thumbnails),
            channelData = channelDomain,
            published = videoDto.snippet.publishedAt.let { ts ->
                timeStampMapper.parseTimestamp(ts)
            },
            isLiveBroadcast = videoDto
                .snippet.liveBroadcastContent
                .let { it == YoutubeVideosDto.LIVE || it == YoutubeVideosDto.UPCOMING },
            isLiveBroadcastUpcoming = videoDto
                .snippet.liveBroadcastContent
                .let { it == YoutubeVideosDto.UPCOMING }
        )
    }

}