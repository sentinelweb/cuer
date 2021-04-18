package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

@Serializable
data class SearchDomain(
    var isLocal: Boolean = true,
    var localParams: LocalParms = LocalParms(),
    var remoteParams: RemoteParms = RemoteParms()
) {

    @Serializable
    data class LocalParms(
        var text: String = "",
        var isWatched: Boolean = true,
        var isNew: Boolean = true,
        var isLive: Boolean = false,
        val playlists: MutableSet<PlaylistDomain> = mutableSetOf()
    )

    @Serializable
    data class RemoteParms(
        var text: String = "",
        var platform: PlatformDomain = PlatformDomain.YOUTUBE,
        var relatedToPlatformId: String? = null,
        var channelPlatformId: String? = null,
        var isLive: Boolean? = null
    )
}