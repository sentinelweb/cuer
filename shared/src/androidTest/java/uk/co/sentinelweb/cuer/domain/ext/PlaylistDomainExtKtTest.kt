package uk.co.sentinelweb.cuer.domain.ext

import kotlinx.datetime.*
import org.junit.Assert.*
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

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

    @Test
    fun isAncestor() {
        assertTrue(treeLookup[2]!!.isAncestor(treeLookup[4]!!))
        assertTrue(tree.isAncestor(treeLookup[2]!!))
    }

    @Test
    fun isDescendent() {
        assertTrue(treeLookup[4]!!.isDescendent(treeLookup[2]!!))
        assertFalse(tree.isDescendent(treeLookup[2]!!))
    }

    @Test
    fun orderIsAscending() {
        val now = Clock.System.now()
        val test = PlaylistDomain
            .createYoutube("plUrl1", "plPlat1")
            .copy(
                items = (0..5).map {
                    PlaylistItemDomain(
                        media = MediaDomain.createYoutube("url$it", "plat$it")
                            .copy(
                                published = now.plus(it, DateTimeUnit.SECOND)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                            ),
                        dateAdded = now,
                        order = it.toLong()
                    )
                }
            )

        assertTrue(test.orderIsAscending())
    }

    @Test
    fun orderIsAscending_false() {
        val now = Clock.System.now()
        val test = PlaylistDomain
            .createYoutube("plUrl1", "plPlat1")
            .copy(
                items = (0..5).map {
                    PlaylistItemDomain(
                        media = MediaDomain.createYoutube("url$it", "plat$it")
                            .copy(
                                published = now.minus(it, DateTimeUnit.SECOND)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                            ),
                        dateAdded = now,
                        order = it.toLong()
                    )
                }
            )

        assertFalse(test.orderIsAscending())
    }
}