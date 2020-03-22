package uk.co.sentinelweb.cuer.ui.queue.dummy

import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object Queue {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: List<QueueItem> = listOf(
        QueueItem("1","https://www.youtube.com/watch?v=52nqjrCs57s","Why You Can't FOCUS - And How To Fix That"),
        QueueItem("2","https://www.youtube.com/watch?v=6a2dLVx8THA","Animating Poststructuralism"),
        QueueItem("3","https://www.youtube.com/watch?v=CkHooEp3vRE","Masters Of Money | Part 1 | John Maynard Keynes"),
        QueueItem("4","https://www.youtube.com/watch?v=EIYqTj402PE","Masters Of Money | Part 2 | Friedrich Hayek"),
        QueueItem("5","https://www.youtube.com/watch?v=oaIpYo3Z5lc","Masters Of Money | Part 3 | Karl Marx"),
        QueueItem("6","https://www.youtube.com/watch?v=OFdgR8Zt084","Order! High Voltage - John Bercow x Electric Six"),
        QueueItem("7","https://www.youtube.com/watch?v=vMiF9Bv-72s","Adorno and Horkheimer: Dialectic of Enlightenment - Part I"),
        QueueItem("8","https://https://www.youtube.com/watch?v=AXyr4Zasdkg","Foucault: Biopower, Governmentality, ∂and the Subject"),
        QueueItem("9","https://www.youtube.com/watch?v=GNGvqjwich0&t=73s","The Marketplace of Ideas: A Critique")
    )

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, QueueItem> = HashMap()

    init {
        ITEMS.forEach {
            addItem(it)
        }
    }

    private fun addItem(item: QueueItem) {
        ITEM_MAP.put(item.id, item)
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class QueueItem(
        val id: String,
        val url: String,
        val title: String) {

        override fun toString(): String = "$title - $url"
    }
}
