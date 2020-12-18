package uk.co.sentinelweb.cuer.app.db.backup.version.v1

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ChannelDomain constructor(
    val id: String = "",
    val title: String? = null,
    val description: String? = null,
    @ContextualSerialization val published: LocalDateTime? = null,
    val thumbNail: ImageDomain? = null,
    val image: ImageDomain? = null,
    val starred: Boolean = false
)