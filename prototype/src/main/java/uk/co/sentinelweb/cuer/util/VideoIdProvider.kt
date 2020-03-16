package uk.co.sentinelweb.cuer.util

import uk.co.sentinelweb.cuer.ui.queue.dummy.Queue

class VideoIdProvider () {
    private var index = 0;

    fun getNextVideoId() = getId(Queue.ITEMS[index].url).also{index ++}

    fun getId(url:String) = url.substring(url.indexOf("?v=")+3)
}