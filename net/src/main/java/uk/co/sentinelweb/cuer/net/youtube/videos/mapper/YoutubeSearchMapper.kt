package uk.co.sentinelweb.cuer.net.youtube.videos.mapper

import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.creator.PlaylistItemCreator
import uk.co.sentinelweb.cuer.net.exception.BadDataException
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeChannelsDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeSearchDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeSearchDto.Companion.EVENT_TYPE_MAP
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeSearchDto.EventType.*
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeSearchDto.Order.RATING
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeSearchDto.Type.VIDEO
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeSearchRequestDto

internal class YoutubeSearchMapper(
    private val timeStampMapper: TimeStampMapper,
    private val timeProvider: TimeProvider,
    private val itemCreator: PlaylistItemCreator,
    private val imageMapper: YoutubeImageMapper,
    private val channelMapper: YoutubeChannelDomainMapper
) {
    fun mapRequest(domain: SearchRemoteDomain) = YoutubeSearchRequestDto(
        q = domain.text,
        type = VIDEO.param,
        relatedToVideoId = domain.relatedToPlatformId,
        channelId = domain.channelPlatformId,
        publishedBefore = domain.toDate?.let { timeStampMapper.mapTimestamp(it) },
        publishedAfter = domain.fromDate?.let { timeStampMapper.mapTimestamp(it) },
        order = RATING.param,
        eventType = domain.isLive
            ?.let {
                (if (it) listOf(LIVE, UPCOMING) else listOf(COMPLETED)).joinToString()
            },
        maxResults = 50,
        pageToken = null,

        )

    fun map(
        it: YoutubeSearchDto,
        channelDtos: List<YoutubeChannelsDto.ItemDto>
    ): PlaylistDomain {
        val channelLookup = channelDtos.associateBy({ it.id }, { channelMapper.map(it) })
        return PlaylistDomain(
            title = "Search results",
            platform = PlatformDomain.YOUTUBE,
            type = PLATFORM,
            config = PlaylistDomain.PlaylistConfigDomain(
                published = timeProvider.localDateTime()
            ),
            currentIndex = -1,
            items = mapItems(it.items, channelLookup)
        )
    }

    private fun mapItems(
        items: List<YoutubeSearchDto.SearchResultDto>,
        channelLookup: Map<String, ChannelDomain>
    ): List<PlaylistItemDomain> = items
        .filter { it.id.kind == VIDEO.kind }
        .mapIndexed { i, item ->
            itemCreator.buildPlayListItem(mapMedia(item, channelLookup), null, order = i * 1000L)
        }

    private fun mapMedia(
        item: YoutubeSearchDto.SearchResultDto,
        channelLookup: Map<String, ChannelDomain>
    ) = item.let {
        MediaDomain(
            id = null,
            url = it.id.videoId?.let { "https://youtu.be/$it" } ?: throw BadDataException("No video ID"),
            title = it.snippet.title,
            description = it.snippet.description,
            mediaType = MediaDomain.MediaTypeDomain.VIDEO,
            platform = PlatformDomain.YOUTUBE,
            platformId = it.id.videoId ?: throw BadDataException("No video ID"),
            duration = -1,
            thumbNail = imageMapper.mapThumb(it.snippet.thumbnails),
            image = imageMapper.mapImage(it.snippet.thumbnails),
            channelData = channelLookup[it.snippet.channelId] ?: throw BadDataException("Channel not found"),
            published = it.snippet.publishedAt.let { ts -> timeStampMapper.mapTimestamp(ts) },
            isLiveBroadcast = (EVENT_TYPE_MAP[it.snippet.liveBroadcastContent] != COMPLETED),
            isLiveBroadcastUpcoming = (EVENT_TYPE_MAP[it.snippet.liveBroadcastContent] == UPCOMING)
        )
    }
}