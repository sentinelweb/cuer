package uk.co.sentinelweb.cuer.ui.queue.dummy

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object Queue {
    /**
     * A dummy item representing a piece of content.
     */
    data class QueueItem(
        val url: String,
        val title: String
    ) {
        override fun toString(): String = "$title - $url"
        fun getId() = url.substring(url.indexOf("?v=") + 3)
    }


    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: List<QueueItem> = listOf(
        QueueItem(
            "https://www.youtube.com/watch?v=c2_t3M_vSsg",
            "Responding to a Pandemic: The Myth of Sisyphus"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=6a2dLVx8THA",
            "Animating Poststructuralism"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=CkHooEp3vRE",
            "Masters Of Money | Part 1 | John Maynard Keynes"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=EIYqTj402PE",
            "Masters Of Money | Part 2 | Friedrich Hayek"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=oaIpYo3Z5lc",
            "Masters Of Money | Part 3 | Karl Marx"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=OFdgR8Zt084",
            "Order! High Voltage - John Bercow x Electric Six"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=vMiF9Bv-72s",
            "Adorno and Horkheimer: Dialectic of Enlightenment - Part I"
        ),
        QueueItem(
            "https://https://www.youtube.com/watch?v=AXyr4Zasdkg",
            "Foucault: Biopower, Governmentality, and the Subject"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=GNGvqjwich0&t=73s",
            "The Marketplace of Ideas: A Critique"
        ),
        QueueItem(
            "https://www.youtube.com/watch?v=52nqjrCs57s",
            "Why You Can't FOCUS - And How To Fix That"
        )
    )

    class VideoProvider() {
        private var index = -1

        fun getNextVideo(): QueueItem {
            index++
            if (index >= ITEMS.size) index = 0
            return ITEMS[index]
        }

        fun getPreviousVideo(): QueueItem {
            index--
            if (index < 0) index = ITEMS.size - 1
            return ITEMS[index]
        }
    }
}
