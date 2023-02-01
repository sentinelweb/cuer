package uk.co.sentinelweb.cuer.domain.ext

import kotlinx.datetime.*
import org.junit.Assert.*
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.GUIDCreator

class PlaylistDomainExtKtTest {
    private val guidCreator = GUIDCreator()

    private val ids = listOf(
        Identifier(guidCreator.create(), LOCAL),
        Identifier(guidCreator.create(), LOCAL),
        Identifier(guidCreator.create(), LOCAL),
        Identifier(guidCreator.create(), LOCAL),
        Identifier(guidCreator.create(), LOCAL),
        Identifier(guidCreator.create(), LOCAL),
        Identifier(guidCreator.create(), LOCAL),
        Identifier(guidCreator.create(), LOCAL),
        Identifier(guidCreator.create(), LOCAL),
    )
    private val nodes = listOf(
        PlaylistDomain(ids[0]/*2*/, "1", parentId = null),
        PlaylistDomain(ids[1]/*3*/, "11", parentId = ids[0]),
        PlaylistDomain(ids[2]/*4*/, "111", parentId = ids[1]),
        PlaylistDomain(ids[3]/*5*/, "2", parentId = null),
        PlaylistDomain(ids[4]/*6*/, "21", parentId = ids[3]),
        PlaylistDomain(ids[5]/*7*/, "211", parentId = ids[4]),
        PlaylistDomain(ids[6]/*8*/, "212", parentId = ids[4]),
        PlaylistDomain(ids[7]/*9*/, "2121", parentId = ids[6]),
        PlaylistDomain(ids[8]/*10*/, "3", parentId = null),
    )

    private val tree = nodes.buildTree()
    private val treeLookup = tree.buildLookup()

    @Test
    fun depth() {
        assertEquals(0, tree.depth())
        assertEquals(4, treeLookup[ids[7]/*9*/]!!.depth())
        assertEquals(1, treeLookup[ids[3]/*5*/]!!.depth())
        assertEquals(2, treeLookup[ids[4]/*6*/]!!.depth())
    }

    @Test
    fun descendents() {
        assertEquals(9, tree.descendents())
        assertEquals(0, treeLookup[ids[7]/*9*/]!!.descendents())
        assertEquals(4, treeLookup[ids[3]/*5*/]!!.descendents())
        assertEquals(3, treeLookup[ids[4]/*6*/]!!.descendents())
    }

    @Test
    fun isAncestor() {
        assertTrue(treeLookup[ids[0]/*2*/]!!.isAncestor(treeLookup[ids[2]/*4*/]!!))
        assertTrue(tree.isAncestor(treeLookup[ids[0]/*2*/]!!))
    }

    @Test
    fun isDescendent() {
        assertTrue(treeLookup[ids[2]/*4*/]!!.isDescendent(treeLookup[ids[0]/*2*/]!!))
        assertFalse(tree.isDescendent(treeLookup[ids[0]/*2*/]!!))
    }

    @Test
    fun orderIsAscending() {
        val now = Clock.System.now()
        val test = PlaylistDomain
            .createYoutube("plUrl1", "plPlat1")
            .copy(
                items = (0..5).map {
                    PlaylistItemDomain(
                        id = null,
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
                        id = null,
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