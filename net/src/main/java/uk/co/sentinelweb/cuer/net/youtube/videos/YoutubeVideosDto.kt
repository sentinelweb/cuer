package uk.co.sentinelweb.cuer.net.youtube.videos

data class YoutubeVideosDto constructor(
    val items: List<VideoDto>
) {
    data class VideoDto constructor(
        val id: String,
        val snippet: SnippetDto?, // optional field (declared as part)
        val contentDetails: ContentDto? // optional field (declared as part)
    ) {

        data class SnippetDto constructor(
            val title: String,
            val description: String,
            val channelId: String,
            val channelTitle: String,
            val publishedAt: String,
            val thumbnails:ThumbnailsDto

        ) {
            data class ThumbnailsDto constructor(
                val default: ThumbnailDto?,
                val medium: ThumbnailDto?,
                val high: ThumbnailDto?,
                val standard: ThumbnailDto?,
                val maxres: ThumbnailDto?
            ) {
                data class ThumbnailDto constructor(
                    val url: String,
                    val width: Int,
                    val height: Int
                )
            }
        }

        data class ContentDto constructor(
            val duration: String,
            val definition: String
        )
    }
}