package uk.co.sentinelweb.cuer.net.youtube.videos

import kotlinx.serialization.Serializable

@Serializable
data class YoutubeVideosDto constructor(
    val items: List<VideoDto>
) {
    @Serializable
    data class VideoDto constructor(
        val id: String,
        val snippet: SnippetDto?, // optional field (declared as part)
        val contentDetails: ContentDto? // optional field (declared as part)
    ) {
        @Serializable
        data class SnippetDto constructor(
            val title: String,
            val description: String,
            val channelId: String,
            val channelTitle: String,
            val publishedAt: String,
            val thumbnails:ThumbnailsDto

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

        @Serializable
        data class ContentDto constructor(
            val duration: String,
            val definition: String
        )
    }
}