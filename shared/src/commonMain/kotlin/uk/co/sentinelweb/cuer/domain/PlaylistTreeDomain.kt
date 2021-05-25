package uk.co.sentinelweb.cuer.domain

data class PlaylistTreeDomain constructor(
    val node: PlaylistDomain? = null,
    var parent: PlaylistTreeDomain? = null,
    val chidren: List<PlaylistTreeDomain> = listOf()
)

data class MutablePlaylistTreeDomain constructor(
    var node: PlaylistDomain?,
    var parent: MutablePlaylistTreeDomain?,
    val chidren: MutableList<MutablePlaylistTreeDomain> = mutableListOf()
)