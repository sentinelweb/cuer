package uk.co.sentinelweb.cuer.net.youtube.videos.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ThumbnailDto constructor(
    val url: String,
    val width: Int,
    val height: Int
)