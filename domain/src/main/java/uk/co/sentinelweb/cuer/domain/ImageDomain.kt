package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

@Serializable
data class ImageDomain constructor(
    val url: String,
    val width: Int? = null,
    val height: Int? = null
)