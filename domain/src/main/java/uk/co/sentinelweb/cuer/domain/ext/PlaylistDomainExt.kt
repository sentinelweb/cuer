package uk.co.sentinelweb.cuer.domain.ext

import uk.co.sentinelweb.cuer.domain.PlaylistDomain

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