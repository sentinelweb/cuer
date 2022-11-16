package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistTreeDomain constructor(
    val node: PlaylistDomain? = null,
    var parent: PlaylistTreeDomain? = null,
    val chidren: List<PlaylistTreeDomain> = listOf()
) : Domain

data class MutablePlaylistTreeDomain constructor(
    var node: PlaylistDomain?,
    var parent: MutablePlaylistTreeDomain?,
    val chidren: MutableList<MutablePlaylistTreeDomain> = mutableListOf()
)