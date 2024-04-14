package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class YoutubeChannelsDto constructor(
    val items: List<ItemDto>
) {
    @Serializable
    internal data class ItemDto constructor(
        val id: String,
        val snippet: SnippetDto
    ) {
        @Serializable
        internal data class SnippetDto constructor(
            val title: String,
            val description: String?,
            val customUrl: String? = null,
            val country: String? = null,
            val publishedAt: String,// todo instant serializer
            val thumbnails: ThumbnailsDto
        )
    }
}