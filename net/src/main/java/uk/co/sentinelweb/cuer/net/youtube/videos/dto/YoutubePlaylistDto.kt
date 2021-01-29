package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

@Serializable
data class YoutubePlaylistDto constructor(
    val items: List<PlaylistDto>
) {
    @Serializable
    data class PlaylistDto constructor(
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
            val thumbnails: ThumbnailsDto
        )

        @Serializable
        data class ContentDto constructor(
            val itemCount: Int
        )
    }
}