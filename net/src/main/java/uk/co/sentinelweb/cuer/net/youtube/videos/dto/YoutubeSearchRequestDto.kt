package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import uk.co.sentinelweb.cuer.net.youtube.YoutubeService

internal data class YoutubeSearchRequestDto(
    val q: String?,
    val type: String,
    val relatedToVideoId: String?,
    val channelId: String?,
    val publishedBefore: String?, // DATE STRING - RFC 3339  (1970-01-01T00:00:00Z)
    val publishedAfter: String?, // DATE STRING - RFC 3339  (1970-01-01T00:00:00Z)
    val order: String,
    val eventType: String?,
    val maxResults: Int = YoutubeService.MAX_RESULTS,
    val pageToken: String? = null,
    val relevanceLanguage: String? = "en"
)