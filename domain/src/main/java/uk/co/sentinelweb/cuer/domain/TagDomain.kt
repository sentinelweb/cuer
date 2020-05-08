package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

@Serializable
data class TagDomain constructor(
    val id: String? = null,
    val tag: String
)