package uk.co.sentinelweb.cuer.domain.ext

import summarise
import uk.co.sentinelweb.cuer.domain.*

fun PlaylistDomain.currentItem() =
    if (currentIndex > -1 && items.size > 0 && currentIndex < items.size) {
        items[currentIndex]
    } else null

fun PlaylistDomain.currentItemOrStart() = if (currentIndex > -1 && currentIndex < items.size) {
    items[currentIndex]
} else if (items.size > 0) {
    items[0]
} else null

fun PlaylistDomain.itemWitId(id: Long?) = items.find { it.id == id }

fun PlaylistDomain.indexOfItem(item: PlaylistItemDomain): Int? {
    return indexOfItemId(item.id)
}

fun PlaylistDomain.indexOfItemId(id1: Long?): Int? {
    return id1?.let {
        val indexOfFirst = items.indexOfFirst { it.id == id1 }
        return if (indexOfFirst > -1) {
            indexOfFirst
        } else null
    }
}

fun PlaylistDomain.matchesHeader(playlist: PlaylistDomain?): Boolean =
    this.copy(items = listOf()) == playlist?.copy(items = listOf())

fun PlaylistDomain.replaceHeader(header: PlaylistDomain) = header.copy(items = items)

fun PlaylistDomain.replaceHeaderKeepIndex(header: PlaylistDomain) =
    header.copy(items = items, currentIndex = currentIndex)

fun PlaylistDomain.removeItem(item: PlaylistItemDomain): PlaylistDomain? =
    this.items
        .find { it.id == item.id }
        ?.let { this.copy(items = this.items.minus(it)) }

fun PlaylistDomain.removeItemByPlatformId(item: PlaylistItemDomain): PlaylistDomain? =
    this.items
        .find { matchPlatform(it, item) }
        ?.let { this.copy(items = this.items.minus(it)) }

fun PlaylistDomain.replaceItem(item: PlaylistItemDomain) =
    this.items.indexOfFirst { it.id == item.id }
        .takeIf { it > -1 }
        ?.let { index ->
            this.copy(items = this.items.toMutableList().apply { set(index, item) }.toList())
        }
        ?: this

fun PlaylistDomain.replaceMediaByPlatformId(media: MediaDomain) =
    this.items.indexOfFirst { it.media.platform == media.platform && it.media.platformId == media.platformId }
        .takeIf { it > -1 }
        ?.let { index ->
            this.copy(
                items = this.items.toMutableList()
                    .apply { set(index, get(index).copy(media = media)) }.toList()
            )
        } ?: this

fun PlaylistDomain.replaceItemByPlatformId(item: PlaylistItemDomain) =
    this.items
        .indexOfFirst { matchPlatform(it, item) }
        .let { index ->
            if (index > -1) {
                this.copy(items = this.items.toMutableList().apply { set(index, item) }.toList())
            } else this
        }

private fun matchPlatform(it: PlaylistItemDomain, item: PlaylistItemDomain) =
    it.media.platformId == item.media.platformId && it.media.platform == item.media.platform

fun PlaylistDomain.scanOrder(): StringBuilder {
    var lastorder = -1L
    val orderString = StringBuilder("cur: $currentIndex [")
    items.forEachIndexed { i, item ->
        orderString.append("$i: ${item.id}-${item.order}")
        if (lastorder > -1 && lastorder >= item.order) {
            orderString.append("*")
        }
        lastorder = item.order
        orderString.append(",")
    }
    orderString
        //.deleteCharAt(orderString.length - 1)
        .append("]")
    return orderString
}

fun PlaylistDomain.isAllWatched() = this.items
    .fold(Pair(/*all unwatched*/true,/*all watched*/ true)) { acc, item ->
        (!item.media.watched && acc.first) to (item.media.watched && acc.second)
    }
    .let { if (it.first && !it.second) false else if (!it.first && it.second) true else null }

fun List<PlaylistDomain>.buildTree(): PlaylistTreeDomain {
    val treeLookup = mutableMapOf<Long?, MutablePlaylistTreeDomain>()
    val root = MutablePlaylistTreeDomain(null, null)
    forEach { pl ->
        val node = MutablePlaylistTreeDomain(pl, null)
        treeLookup.put(pl.id, node)
    }
    forEach { pl ->
        val node = treeLookup[pl.id]!!
        pl.parentId?.let { parentId ->
            treeLookup[parentId]
                ?.apply { chidren.add(node) }
                ?.apply { node.parent = this }
                ?: also {
                    root.chidren.add(node)
                    node.parent = root
                }
        } ?: also {
            root.chidren.add(node)
            node.parent = root
        }
    }
    return root.toImmutableTree()
}

fun MutablePlaylistTreeDomain.toImmutableTree(): PlaylistTreeDomain =
    PlaylistTreeDomain(this.node, null, this.chidren.map { it.toImmutableTree() })
        .also { t -> t.chidren.forEach { it.parent = t } }

fun PlaylistTreeDomain.toMutableTree(): MutablePlaylistTreeDomain =
    MutablePlaylistTreeDomain(
        this.node,
        null,
        this.chidren.map { it.toMutableTree() }.toMutableList()
    )
        .also { t -> t.chidren.forEach { it.parent = t } }

fun PlaylistTreeDomain.depth(): Int = (this.parent?.depth()?.inc() ?: 0)

fun PlaylistTreeDomain.descendents(): Int =
    this.chidren.size + this.chidren.sumOf { it.descendents() }

fun PlaylistTreeDomain.buildLookup(): Map<Long, PlaylistTreeDomain> =
    this.chidren.associateBy { it.node?.id!! }.toMutableMap()
        .also { map -> this.chidren.forEach { map.putAll(it.buildLookup()) } }

fun PlaylistTreeDomain.dumpTree(pfx: String = "") {
    chidren.forEach { it.dumpTree("-$pfx") }
}

fun PlaylistTreeDomain.iterate(depth: Int = 0, cb: (pl: PlaylistTreeDomain, Int) -> Unit) {
    cb(this, depth)
    chidren.forEach { it.iterate(depth + 1, cb) }
}

fun PlaylistTreeDomain.sort(comp: Comparator<PlaylistTreeDomain>): PlaylistTreeDomain {
    return copy(chidren = chidren
        .sortedWith(comp)
        .map { td -> td.sort(comp) })
}

fun PlaylistTreeDomain.isAncestor(check: PlaylistTreeDomain): Boolean {
    if (node == null) return true // root node
    var start = check
    while (start.node != null) {
        if (start.node?.id == this.node.id) {
            return true
        }
        start = start.parent!!
    }
    return false
}

fun PlaylistTreeDomain.isDescendent(check: PlaylistTreeDomain): Boolean {
    if (node == null) return false
    var start = this
    while (start.node != null) {
        if (start.node?.id == check.node?.id) {
            return true
        }
        start = start.parent!!
    }
    return false
}

fun PlaylistDomain.summarise(): String = """
    id: $id, platform: $platform - $platformId, type: $type, title: $title, 
    items:${items.map { it.summarise() }.joinToString("\n")}
""".trimIndent()
