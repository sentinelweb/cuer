package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

@Serializable
data class YoutubeVideosDto constructor(
    val items: List<VideoDto>
) {
    @Serializable
    data class VideoDto constructor(
        val id: String,
        val snippet: SnippetDto?, // optional field (declared as part)
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

        ) {
            @Serializable
            data class ThumbnailsDto constructor(
                val default: ThumbnailDto?,
                val medium: ThumbnailDto?,
                val high: ThumbnailDto?,
                val standard: ThumbnailDto?,
                val maxres: ThumbnailDto?
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