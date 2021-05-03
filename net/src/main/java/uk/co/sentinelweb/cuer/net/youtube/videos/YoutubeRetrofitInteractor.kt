package uk.co.sentinelweb.cuer.net.youtube.videos

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.core.ntuple.then
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.exception.InvalidPartsException
import uk.co.sentinelweb.cuer.net.retrofit.ErrorMapper
import uk.co.sentinelweb.cuer.net.youtube.YoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService.Companion.MAX_RESULTS
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.CONTENT_DETAILS
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.SNIPPET
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeChannelsDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubePlaylistDto.Companion.MAX_PLAYLIST_ITEMS
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubePlaylistItemDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeChannelDomainMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubePlaylistDomainMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeSearchMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeVideoMediaDomainMapper

/**
 * Youtube interactor implementation
 * todo categories : https://www.googleapis.com/youtube/v3/videoCategories?regionCode=uk&key=
 */
internal class YoutubeRetrofitInteractor constructor(
    private val keyProvider: YoutubeApiKeyProvider,
    private val service: YoutubeService,
    private val videoMapper: YoutubeVideoMediaDomainMapper,
    private val channelMapper: YoutubeChannelDomainMapper,
    private val playlistMapper: YoutubePlaylistDomainMapper,
    private val searchMapper: YoutubeSearchMapper,
    private val coContext: CoroutineContextProvider,
    private val errorMapper: ErrorMapper,
    private val connectivity: ConnectivityWrapper
) : YoutubeInteractor {

    init {
        errorMapper.log.tag(this)
    }

    suspend override fun videos(
        ids: List<String>,
        parts: List<YoutubePart>
    ): NetResult<List<MediaDomain>> =
        withContext(coContext.IO) {
            try {
                if (connectivity.isConnected()) {
                    if (!parts.contains(SNIPPET)) {
                        throw InvalidPartsException(SNIPPET)
                    }
                    service.getVideoInfos(
                        ids = ids.joinToString(separator = ","),
                        parts = parts.map { it.part }.joinToString(separator = ","),
                        key = keyProvider.key
                    )
                        .let { videoMapper.map(it) }
                        .let { medias ->
                            updateChannelData(medias)
                                .takeIf { it.isSuccessful }
                                .let { it?.data }
                                ?: medias
                        }
                        .let { NetResult.Data(it) }
                } else {
                    errorMapper.notConnected<List<MediaDomain>>()
                }
            } catch (ex: Throwable) {
                errorMapper.map<List<MediaDomain>>(ex, "videos: error: $ids")
            }
        }

    // todo add existing channel provider to see if channel is already in db
    suspend fun updateChannelData(
        medias: List<MediaDomain>
    ): NetResult<List<MediaDomain>> =
        withContext(coContext.IO) {
            val idList = medias.map { it.channelData.platformId!! }.distinct()
            try {
                if (connectivity.isConnected()) {
                    // note the items come out of order
                    channels(ids = idList)
                        .takeIf { it.isSuccessful }
                        ?.data
                        ?.let { channels ->
                            medias.map { media ->
                                channels
                                    .find { channel -> channel.id == media.channelData.id }
                                    ?.let { media.copy(channelData = it) }
                                    ?: media
                            }
                        }?.let { NetResult.Data(it) }
                        ?: medias
                            .let { NetResult.Data(it) }
                } else {
                    errorMapper.notConnected<List<MediaDomain>>()
                }
            } catch (ex: Throwable) {
                errorMapper.map<List<MediaDomain>>(ex, "updateChannelData: error: $idList")
            }
        }

    /**
     * todo add existing channel provider to see if channel is already in db
     * note the items come out of order from the API
     */
    override suspend fun channels(
        ids: List<String>,
        parts: List<YoutubePart>
    ): NetResult<List<ChannelDomain>> =
        withContext(coContext.IO) {
            try {
                if (connectivity.isConnected()) {
                    if (!parts.contains(SNIPPET)) {
                        throw InvalidPartsException(SNIPPET)
                    }
                    service.getChannelInfos(
                        ids = ids.joinToString(separator = ","),
                        parts = parts.map { it.part }.joinToString(separator = ","),
                        key = keyProvider.key
                    )
                        .let { channelMapper.map(it) }
                        .let { NetResult.Data(it) }
                } else {
                    errorMapper.notConnected<List<ChannelDomain>>()
                }
            } catch (ex: Throwable) {
                errorMapper.map<List<ChannelDomain>>(ex, "channels: error: $ids")
            }
        }

    override suspend fun playlist(id: String): NetResult<PlaylistDomain> =
        withContext(coContext.IO) {
            try {
                if (connectivity.isConnected()) {
                    service.getPlaylistInfos(
                        ids = id,
                        parts = listOf(SNIPPET, CONTENT_DETAILS).map { it.part }.joinToString(separator = ","),
                        key = keyProvider.key
                    ).let { playlistDto ->
                        // get items
                        val itemsDtoList = mutableListOf<YoutubePlaylistItemDto.PlaylistItemDto>()
                        var hasMorePages: Boolean = true
                        var nextToken: String? = null
                        while (hasMorePages) {
                            service.getPlaylistItemInfos(
                                playlistId = id,
                                parts = listOf(SNIPPET)
                                    .map { it.part }
                                    .joinToString(separator = ","),
                                key = keyProvider.key,
                                pageToken = nextToken,
                                maxResults = MAX_RESULTS
                            ).apply {
                                itemsDtoList.addAll(items)
                                hasMorePages = nextPageToken != null && itemsDtoList.size < MAX_PLAYLIST_ITEMS
                                nextToken = this.nextPageToken
                            }
                        }
                        // get videos
                        val videosDtoList = mutableListOf<YoutubeVideosDto.VideoDto>()
                        itemsDtoList.map { it.snippet.resourceId.videoId }
                            .chunked(MAX_RESULTS)
                            .forEach {
                                service.getVideoInfos(
                                    ids = it.joinToString(separator = ","),
                                    parts = listOf(SNIPPET, CONTENT_DETAILS)
                                        .map { it.part }
                                        .joinToString(separator = ","),
                                    key = keyProvider.key
                                ).apply {
                                    videosDtoList.addAll(items)
                                }
                            }
                        val channelsDtoList = mutableListOf<YoutubeChannelsDto.ItemDto>()
                        listOf(playlistDto.items[0].snippet.channelId)
                            .plus(videosDtoList.map { it.snippet.channelId })
                            .distinct()
                            .chunked(MAX_RESULTS)
                            .forEach {
                                service.getChannelInfos(
                                    ids = it.joinToString(separator = ","),
                                    parts = listOf(SNIPPET).map { it.part }
                                        .joinToString(separator = ","),
                                    key = keyProvider.key
                                ).apply {
                                    channelsDtoList.addAll(items)
                                }
                            }
                        playlistDto to itemsDtoList then videosDtoList then channelsDtoList
                        // todo filter out existing media ids - provide db accessor - then do media fetch
                        // can just exec videos with contentDetails,liveStreamingDetails as all other info is in item
                        // https://www.googleapis.com/youtube/v3/videos?id=8nhPVOM97Jg%2CfY7M3pzXdUo%2CGXfsI-zZO7s&part=contentDetails%2CliveStreamingDetails&key=
                    }
                        .let { playlistMapper.map(it.first, it.second, it.third, it.fourth) }
                        .let { NetResult.Data(it) }
                } else {
                    errorMapper.notConnected<PlaylistDomain>()
                }
            } catch (ex: Throwable) {
                errorMapper.map<PlaylistDomain>(ex, "playlist: error: $id")
            }
        }

    override suspend fun search(search: SearchRemoteDomain): NetResult<PlaylistDomain> =
        withContext(coContext.IO) {
            try {
                if (connectivity.isConnected()) {
                    searchMapper.mapRequest(search)
                        .let {
                            service.search(
                                q = it.q,
                                type = it.type,
                                relatedToVideoId = it.relatedToVideoId,
                                channelId = it.channelId,
                                publishedBefore = it.publishedBefore,
                                publishedAfter = it.publishedAfter,
                                order = it.order,
                                eventType = it.eventType,
                                maxResults = it.maxResults,
                                pageToken = null,
                                key = keyProvider.key
                            )
                        }
                        .let { searchResult ->
                            val channels = searchResult.items.map { it.snippet.channelId }
                                .distinct()
                                .takeIf { it.size > 0 }
                                ?.let {
                                    service.getChannelInfos(
                                        ids = it.joinToString(separator = ","),
                                        parts = listOf(SNIPPET).map { it.part }
                                            .joinToString(separator = ","),
                                        key = keyProvider.key
                                    )
                                }?.items
                                ?: listOf()
                            searchResult to channels
                        }
                        .let { searchMapper.map(it.first, it.second) }
                        .let { NetResult.Data(it) }
                } else {
                    errorMapper.notConnected<PlaylistDomain>()
                }
            } catch (ex: Throwable) {
                errorMapper.map<PlaylistDomain>(ex, "search: error: ${search.text}")
            }
        }

}