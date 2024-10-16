package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain

@Serializable
internal data class YoutubeSearchDto constructor(
    val items: List<SearchResultDto>,
    val nextPageToken: String? = null,
    val prevPageToken: String? = null,
    val pageInfo: PageInfoDto? = null
) {
    @Serializable
    internal data class SearchResultDto constructor(
        val kind: String,
        val id: IdDto,
        val snippet: SnippetDto?,
    ) {
        @Serializable
        internal data class IdDto constructor(
            val kind: String,
            val videoId: String?,
            val playlistId: String? = null,
            val channelId: String? = null
        )

        @Serializable
        internal data class SnippetDto constructor(
            val title: String,
            val description: String,
            val channelId: String,
            val channelTitle: String,
            val publishedAt: String,
            val liveBroadcastContent: String,
            val thumbnails: ThumbnailsDto,
            val publishTime: String
        )

    }

    @Serializable
    internal data class PageInfoDto constructor(
        val totalResults: Int,
        val resultsPerPage: Int
    )

    internal enum class Type(val param: String, val kind: String, val domain: ObjectTypeDomain) {
        VIDEO("video", "youtube#video", ObjectTypeDomain.MEDIA),
        CHANNEL("channel", "youtube#channel", ObjectTypeDomain.CHANNEL),
        PLAYLIST("playlist", "youtube#playlist", ObjectTypeDomain.PLAYLIST),
    }

    internal enum class EventType(val param: String) {
        NONE("none"),
        COMPLETED("completed"),
        LIVE("live"),
        UPCOMING("upcoming")
    }

    companion object {
        internal val EVENT_TYPE_MAP = EventType.values().associateBy { it.param }
    }

}