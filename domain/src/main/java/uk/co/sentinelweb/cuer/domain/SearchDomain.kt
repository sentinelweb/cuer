package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

@Serializable
data class SearchDomain(
    var text: String = "",
    var isLocal: Boolean = true,
    var localParams: LocalParms = LocalParms(),
    var remoteParams: RemoteParms = RemoteParms()
) {

    @Serializable
    data class LocalParms(
        val isWatched: Boolean = true,
        val isNew: Boolean = true,
        val isLive: Boolean = false,
        val playlists: MutableSet<PlaylistDomain> = mutableSetOf()
    )

    @Serializable
    data class RemoteParms(
        val platform: PlatformDomain = PlatformDomain.YOUTUBE,
        val relatedToPlatformId: String? = null,
        val channelPlatformId: String? = null,
        val isLive: Boolean? = null
    )
}