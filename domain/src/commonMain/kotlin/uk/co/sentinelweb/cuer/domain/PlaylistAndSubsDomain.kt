package uk.co.sentinelweb.cuer.domain

data class PlaylistAndSubsDomain(
    val playlist: PlaylistDomain,
    val subPlaylists: List<PlaylistDomain> = listOf(),
)