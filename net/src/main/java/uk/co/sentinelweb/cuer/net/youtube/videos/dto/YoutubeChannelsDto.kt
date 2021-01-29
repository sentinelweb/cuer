package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

@Serializable
data class YoutubeChannelsDto constructor(
    val items: List<ItemDto>
) {
    @Serializable
    data class ItemDto constructor(
        val id: String,
        val snippet: SnippetDto
    ) {
        @Serializable
        data class SnippetDto constructor(
            val title: String,
            val description: String?,
            val customUrl: String? = null,
            val country: String? = null,
            val publishedAt: String,
            val thumbnails: ThumbnailsDto
        )
    }
}