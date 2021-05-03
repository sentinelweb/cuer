package uk.co.sentinelweb.cuer.net.youtube

import retrofit2.http.GET
import retrofit2.http.Query
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.*

internal interface YoutubeService {
    // doc page : https://developers.google.com/youtube/v3/docs/videos/list
    // e.g. : https://www.googleapis.com/youtube/v3/videos?part=id,player,contentDetails,snippet,statistics,recordingDetails&id=8nhPVOM97Jg,fY7M3pzXdUo&key=xxx
    @GET("videos")
    suspend fun getVideoInfos(
        @Query("id") ids: String,
        @Query("part") parts: String,
        @Query("key") key: String,
        @Query("maxResults") maxResults: Int = MAX_RESULTS,
        @Query("pageToken") pageToken: String? = null
    ): YoutubeVideosDto

    @GET("channels")
    suspend fun getChannelInfos(
        @Query("id") ids: String,
        @Query("part") parts: String,
        @Query("key") key: String
    ): YoutubeChannelsDto

    @GET("playlists")
    suspend fun getPlaylistInfos(
        @Query("id") ids: String,
        @Query("part") parts: String,
        @Query("key") key: String
    ): YoutubePlaylistDto

    @GET("playlistItems")
    suspend fun getPlaylistItemInfos(
        @Query("playlistId") playlistId: String,
        @Query("part") parts: String,
        @Query("key") key: String,
        @Query("maxResults") maxResults: Int = MAX_RESULTS,
        @Query("pageToken") pageToken: String? = null
    ): YoutubePlaylistItemDto

    // doc: https://developers.google.com/youtube/v3/docs/search/list
    // https://www.googleapis.com/youtube/v3/search?q=heidigger&type=video&part=snippet&maxResults=50&order=rating&eventType=live&&key=
    @GET("search?part=snippet")
    suspend fun search(
        @Query("q") q: String?,
        @Query("type") type: String,
        @Query("relatedToVideoId") relatedToVideoId: String?,
        @Query("channelId") channelId: String?,
        @Query("publishedBefore") publishedBefore: String?, // DATE STRING
        @Query("publishedAfter") publishedAfter: String?,// DATE STRING
        @Query("order") order: String,
        @Query("eventType") eventType: String?,
        @Query("relevanceLanguage") relevanceLanguage: String = "en",
        @Query("key") key: String,
        @Query("maxResults") maxResults: Int = MAX_RESULTS,
        @Query("pageToken") pageToken: String? = null
    ): YoutubeSearchDto

    companion object {
        val MAX_RESULTS = 50
    }
}