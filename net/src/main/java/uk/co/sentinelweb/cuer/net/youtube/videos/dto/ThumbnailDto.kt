package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

@Serializable
data class ThumbnailDto constructor(
    val url: String,
    val width: Int,
    val height: Int
)