package uk.co.sentinelweb.cuer.net.youtube

import uk.co.sentinelweb.cuer.net.client.ServiceExecutor
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.*

internal class YoutubeService(
    private val executor: ServiceExecutor
) {
    // doc page : https://developers.google.com/youtube/v3/docs/videos/list
    // e.g. : https://www.googleapis.com/youtube/v3/videos?part=id,player,contentDetails,snippet,statistics,recordingDetails&id=8nhPVOM97Jg,fY7M3pzXdUo&key=xxx
    //    @GET("videos")
    internal suspend fun getVideoInfos(
        ids: String,
        parts: String,
        key: String,
        maxResults: Int = MAX_RESULTS,
        pageToken: String? = null
    ): YoutubeVideosDto = executor.get(
        path = "videos",
        urlParams = mapOf(
            "id" to ids,
            "part" to parts,
            "key" to key,
            "maxResults" to maxResults,
            "pageToken" to pageToken,
        )
    )

    //    @GET("channels")
    internal suspend fun getChannelInfos(
        ids: String,
        parts: String,
        key: String
    ): YoutubeChannelsDto = executor.get(
        path = "channels",
        urlParams = mapOf(
            "id" to ids,
            "part" to parts,
            "key" to key,
        )
    )

    //    @GET("playlists")
    internal suspend fun getPlaylistInfos(
        ids: String,
        parts: String,
        key: String
    ): YoutubePlaylistDto = executor.get(
        path = "playlists",
        urlParams = mapOf(
            "id" to ids,
            "part" to parts,
            "key" to key,
        )
    )

    //    @GET("playlistItems")
    internal suspend fun getPlaylistItemInfos(
        playlistId: String,
        parts: String,
        key: String,
        maxResults: Int = MAX_RESULTS,
        pageToken: String? = null
    ): YoutubePlaylistItemDto = executor.get<YoutubePlaylistItemDto>(
        path = "playlistItems",
        urlParams = mapOf(
            "playlistId" to playlistId,
            "part" to parts,
            "key" to key,
            "maxResults" to maxResults,
            "pageToken" to pageToken,
        )
    )

    // doc: https://developers.google.com/youtube/v3/docs/search/list
    // eg: https://www.googleapis.com/youtube/v3/search?q=heidigger&type=video&part=snippet&maxResults=50&order=rating&eventType=live&&key=
    //    @GET("search?part=snippet")
    internal suspend fun search(
        request: YoutubeSearchRequestDto,
        key: String,
    ): YoutubeSearchDto = executor.get<YoutubeSearchDto>(
        path = "search?part=snippet",
        urlParams = mapOf(
            "q" to request.q,
            "type" to request.type,
            "relatedToVideoId" to request.relatedToVideoId,
            "channelId" to request.channelId,
            "publishedBefore" to request.publishedBefore,
            "publishedAfter" to request.publishedAfter,
            "order" to request.order,
            "eventType" to request.eventType,
            "relevanceLanguage" to request.relevanceLanguage,
            "key" to key,
            "maxResults" to request.maxResults,
            "pageToken" to request.pageToken,
        )
    )

    companion object {
        val MAX_RESULTS = 50
    }
}
