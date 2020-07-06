package uk.co.sentinelweb.cuer.app.db.backup.version.v1

import kotlinx.serialization.Serializable

@Serializable
data class TagDomain constructor(
    val id: String? = null,
    val tag: String
)