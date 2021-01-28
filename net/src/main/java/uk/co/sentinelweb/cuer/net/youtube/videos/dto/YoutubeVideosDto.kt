package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

@Serializable
data class YoutubeVideosDto constructor(
    val items: List<VideoDto>,
    val nextPageToken: String?,
    val prevPageToken: String?,
    val pageInfo: PageInfoDto
) {
    @Serializable
    data class VideoDto constructor(
        val id: String,
        val snippet: SnippetDto, // optional field (declared as part)
        val contentDetails: ContentDto? = null// optional field (declared as part)
    ) {
        @Serializable
        data class SnippetDto constructor(
            val title: String,
            val description: String,
            val channelId: String,
            val channelTitle: String,
            val publishedAt: String,
            val liveBroadcastContent: String,
            val thumbnails: ThumbnailsDto

        )

        @Serializable
        data class ContentDto constructor(
            val duration: String,
            val definition: String
        )
    }

    @Serializable
    data class PageInfoDto constructor(
        val totalResults: Int,
        val resultsPerPage: Int
    )

    companion object {
        val LIVE = "live"
        val UPCOMING = "upcoming"
    }
}