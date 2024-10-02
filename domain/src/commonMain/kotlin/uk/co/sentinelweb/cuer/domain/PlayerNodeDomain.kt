package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

// the playback options on a node
@Serializable
data class PlayerNodeDomain(
    val screens: List<Screen>
) : Domain {

    @Serializable
    data class Screen(
        val index: Int,
        val width: Int,
        val height: Int,
        val refreshRate: Int,
        val name: String,
    )
}