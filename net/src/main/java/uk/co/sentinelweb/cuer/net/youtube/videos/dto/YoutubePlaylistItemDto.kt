package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

// don't need contentDetails,status - just snippet
@Serializable
data class YoutubePlaylistItemDto constructor(
    val items: List<PlaylistItemDto>,
    val nextPageToken: String?,
    val prevPageToken: String?,
    val pageInfo: PageInfoDto
) {
    @Serializable
    data class PlaylistItemDto constructor(
        val id: String,
        val snippet: SnippetDto, // shouldn't be optional (though stil declared as part)
    ) {
        @Serializable
        data class SnippetDto constructor(
            val title: String,
            val description: String,
            val channelId: String,
            val channelTitle: String,
            val publishedAt: String,
            val position: Int,
            val thumbnails: ThumbnailsDto,
            val resourceId: ResourceDto
        ) {

            @Serializable
            data class ResourceDto constructor(
                val kind: String,
                val videoId: String
            )
        }

    }

    @Serializable
    data class PageInfoDto constructor(
        val totalResults: Int,
        val resultsPerPage: Int
    )
}