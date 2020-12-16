package uk.co.sentinelweb.cuer.domain

// todo make assembler/creator
data class PlaylistTreeDomain constructor(
    val nodes: List<Node>
) {
    data class Node constructor(
        val parent: PlaylistDomain,
        val children: List<Node>
    )
}