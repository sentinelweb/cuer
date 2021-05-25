package uk.co.sentinelweb.cuer.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.USER

//import java.time.Instant
//import java.time.LocalDateTime

@Serializable
data class PlaylistDomain constructor(
    val id: Long? = null,
    val title: String,
    val items: List<PlaylistItemDomain> = listOf(),
    val currentIndex: Int = 0, // todo make nullable
    val parentId: Long? = null,
    val mode: PlaylistModeDomain = SINGLE,
    val type: PlaylistTypeDomain = USER,
    val platform: PlatformDomain? = null,
    val channelData: ChannelDomain? = null,
    val platformId: String? = null,
    val starred: Boolean = false,
    val archived: Boolean = false,
    val default: Boolean = false,
    val thumb: ImageDomain? = null,
    val image: ImageDomain? = null,
    val playItemsFromStart: Boolean = false,
    val config: PlaylistConfigDomain = PlaylistConfigDomain()
) {

    enum class PlaylistModeDomain {
        SINGLE, LOOP, SHUFFLE
    }

    enum class PlaylistTypeDomain {
        APP,
        USER,
        PLATFORM /* External playlist do allow adding - just fix to source - maybe allow archiving of items */
    }

    @Serializable
    data class PlaylistConfigDomain constructor(
        val updateUrl: String? = null,
        val platformUrl: String? = null,
        @Contextual val lastUpdate: Instant? = null,
        @Contextual val published: LocalDateTime? = null,
        val updateInterval: Long? = null,
        val description: String? = null,
        val playable: Boolean = true,
        val editable: Boolean = true,
        val deletable: Boolean = true,
        val editableItems: Boolean = true,
        val deletableItems: Boolean = true
    )
}

