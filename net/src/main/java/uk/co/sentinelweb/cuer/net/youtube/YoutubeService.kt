package uk.co.sentinelweb.cuer.net.youtube

import retrofit2.http.GET
import retrofit2.http.Query
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeChannelsDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubePlaylistDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubePlaylistItemDto
import uk.co.sentinelweb.cuer.net.youtube.videos.dto.YoutubeVideosDto

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

    companion object {
        val MAX_RESULTS = 50
    }
}