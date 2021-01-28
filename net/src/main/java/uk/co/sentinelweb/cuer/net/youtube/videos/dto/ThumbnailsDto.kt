package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

@Serializable
data class ThumbnailsDto constructor(
    val default: ThumbnailDto? = null,
    val medium: ThumbnailDto? = null,
    val high: ThumbnailDto? = null,
    val standard: ThumbnailDto? = null,
    val maxres: ThumbnailDto? = null
)