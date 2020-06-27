package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
class ChannelDomain constructor(
    val id: String = "",
    val remoteId: String = "",
    val platform: PlatformDomain = PlatformDomain.YOUTUBE,
    val country: String? = null,
    val title: String? = null,
    val customUrl: String? = null,
    val description: String? = null,
    @ContextualSerialization val published: LocalDateTime? = null,
    val thumbNail: ImageDomain? = null,
    val image: ImageDomain? = null,
    val starred: Boolean = false
)