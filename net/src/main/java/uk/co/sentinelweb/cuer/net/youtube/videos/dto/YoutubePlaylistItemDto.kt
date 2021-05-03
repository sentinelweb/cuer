package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

// don't need contentDetails,status - just snippet
@Serializable
internal data class YoutubePlaylistItemDto constructor(
    val items: List<PlaylistItemDto>,
    val nextPageToken: String?,
    val prevPageToken: String?,
    val pageInfo: PageInfoDto
) {
    @Serializable
    internal data class PlaylistItemDto constructor(
        val id: String,
        val snippet: SnippetDto, // shouldn't be optional (though stil declared as part)
    ) {
        @Serializable
        internal data class SnippetDto constructor(
            val title: String,
            val description: String,
            val channelId: String,
            val channelTitle: String,
            val publishedAt: String,
            val position: Int,
            val thumbnails: ThumbnailsDto,
            val videoOwnerChannelTitle: String,
            val videoOwnerChannelId: String,
            val resourceId: ResourceDto
        ) {

            @Serializable
            internal data class ResourceDto constructor(
                val kind: String,
                val videoId: String
            )
        }

    }

    @Serializable
    internal data class PageInfoDto constructor(
        val totalResults: Int,
        val resultsPerPage: Int
    )
}