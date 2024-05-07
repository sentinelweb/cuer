package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistAndSubsDomain(
    val playlist: PlaylistDomain,
    val subPlaylists: List<PlaylistDomain> = listOf(),
) : Domain