package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class YoutubeVideosDto constructor(
    val items: List<VideoDto>,
    val nextPageToken: String? = null,
    val prevPageToken: String? = null,
    val pageInfo: PageInfoDto
) {
    @Serializable
    internal data class VideoDto constructor(
        val id: String,
        val snippet: SnippetDto, // optional field (declared as part)
        val contentDetails: ContentDto? = null// optional field (declared as part)
    ) {
        @Serializable
        internal data class SnippetDto constructor(
            val title: String,
            val description: String,
            val channelId: String,
            val channelTitle: String,
            val publishedAt: String,
            val liveBroadcastContent: String,
            val thumbnails: ThumbnailsDto

        )

        @Serializable
        internal data class ContentDto constructor(
            val duration: String,
            val definition: String
        )
    }

    @Serializable
    internal data class PageInfoDto constructor(
        val totalResults: Int,
        val resultsPerPage: Int
    )

    internal companion object {
        val LIVE = "live"
        val UPCOMING = "upcoming"
    }
}