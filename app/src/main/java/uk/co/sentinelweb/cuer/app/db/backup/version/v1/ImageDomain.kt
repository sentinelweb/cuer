package uk.co.sentinelweb.cuer.app.db.backup.version.v1

import kotlinx.serialization.Serializable

@Serializable
data class ImageDomain constructor(
    val url: String,
    val width: Int?,
    val height: Int?
)