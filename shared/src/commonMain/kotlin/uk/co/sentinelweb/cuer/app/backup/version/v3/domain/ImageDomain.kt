package uk.co.sentinelweb.cuer.app.backup.version.v3.domain

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.Domain

@Serializable
data class ImageDomain constructor(
    val id: Long? = null,
    val url: String,
    val width: Int? = null,
    val height: Int? = null
) : Domain