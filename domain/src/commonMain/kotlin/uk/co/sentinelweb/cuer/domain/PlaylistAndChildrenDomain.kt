package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistAndChildrenDomain(
    val playlist: PlaylistDomain,
    val children: List<PlaylistDomain> = listOf(),
) : Domain