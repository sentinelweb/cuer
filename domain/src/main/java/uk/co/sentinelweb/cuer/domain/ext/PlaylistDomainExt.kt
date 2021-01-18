package uk.co.sentinelweb.cuer.domain.ext

import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

fun PlaylistDomain.currentItem() = if (currentIndex > -1 && items.size > 0 && currentIndex < items.size) {
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

fun PlaylistDomain.replaceHeader(header: PlaylistDomain) = header.copy(items = items)

fun PlaylistDomain.replaceHeaderKeepIndex(header: PlaylistDomain) = header.copy(items = items, currentIndex = currentIndex)

fun PlaylistDomain.removeItem(item: PlaylistItemDomain) =
    this.items.find { it.id == item.id }
        ?.let { this.copy(items = this.items.minus(it)) }

fun PlaylistDomain.replaceItem(item: PlaylistItemDomain) =
    this.items.indexOfFirst { it.id == item.id }
        .let { index ->
            if (index > -1) {
                this.copy(items = this.items.toMutableList().apply { set(index, item) }.toList())
            } else this
        }

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
        .deleteCharAt(orderString.length - 1)
        .append("]")
    return orderString
}

