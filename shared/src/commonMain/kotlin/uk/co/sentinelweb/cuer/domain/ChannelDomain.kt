package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ChannelDomain constructor(
    val id: Long? = null,
    val platformId: String?,
    val platform: PlatformDomain,
    val country: String? = null,
    val title: String? = null,
    val customUrl: String? = null,
    val description: String? = null,
    @Contextual val published: LocalDateTime? = null,
    val thumbNail: ImageDomain? = null,
    val image: ImageDomain? = null,
    val starred: Boolean = false
)