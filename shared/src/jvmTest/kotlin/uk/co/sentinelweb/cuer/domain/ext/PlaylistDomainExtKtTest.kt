package uk.co.sentinelweb.cuer.domain.ext

import org.junit.Assert.assertEquals
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistDomainExtKtTest {
    private val nodes = listOf(
        PlaylistDomain(2, "1", parentId = null),
        PlaylistDomain(3, "11", parentId = 2),
        PlaylistDomain(4, "111", parentId = 3),
        PlaylistDomain(5, "2", parentId = null),
        PlaylistDomain(6, "21", parentId = 5),
        PlaylistDomain(7, "211", parentId = 6),
        PlaylistDomain(8, "212", parentId = 6),
        PlaylistDomain(9, "2121", parentId = 8),
        PlaylistDomain(10, "3", parentId = null),
    )

    private val tree = nodes.buildTree()
    private val treeLookup = tree.buildLookup()

    @Test
    fun depth() {
        assertEquals(0, tree.depth())
        assertEquals(4, treeLookup[9]!!.depth())
        assertEquals(1, treeLookup[5]!!.depth())
        assertEquals(2, treeLookup[6]!!.depth())
    }

    @Test
    fun descendents() {
        assertEquals(9, tree.descendents())
        assertEquals(0, treeLookup[9]!!.descendents())
        assertEquals(4, treeLookup[5]!!.descendents())
        assertEquals(3, treeLookup[6]!!.descendents())
    }

}