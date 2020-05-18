package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

@Serializable
data class YoutubeChannelsDto constructor(
    val items: List<ItemDto>
) {
    @Serializable
    data class ItemDto constructor(
        val id: String,
        val snippet: SnippetDto? = null // optional field (declared as part)
    ) {
        @Serializable
        data class SnippetDto constructor(
            val title: String,
            val description: String?,
            val customUrl: String?,
            val country: String,
            val publishedAt: String,
            val thumbnails: ThumbnailsDto
        ) {
            @Serializable
            data class ThumbnailsDto constructor(
                val default: ThumbnailDto? = null,
                val medium: ThumbnailDto? = null,
                val high: ThumbnailDto? = null,
                val standard: ThumbnailDto? = null,
                val maxres: ThumbnailDto? = null
            ) {
                @Serializable
                data class ThumbnailDto constructor(
                    val url: String,
                    val width: Int,
                    val height: Int
                )
            }
        }
    }
}