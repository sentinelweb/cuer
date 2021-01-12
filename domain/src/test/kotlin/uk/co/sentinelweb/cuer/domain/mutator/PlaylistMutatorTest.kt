package uk.co.sentinelweb.cuer.domain.mutator

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.JFixture
import com.flextrade.jfixture.annotations.Fixture
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.LOOP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.scanOrder
import java.lang.Integer.max

class PlaylistMutatorTest {

    @Fixture
    private lateinit var fixtPlaylist: PlaylistDomain

    private val sut = PlaylistMutator()
    private val fixture = JFixture()

    @Before
    fun setUp() {
        FixtureAnnotations.initFixtures(this)
        fixtPlaylist = fixtPlaylist.copy(
            items = (0..10).map { idx ->
                fixture.create(PlaylistItemDomain::class.java).copy(
                    order = 10000L + (idx * 1000L)
                )
            }
        )
        println("------- start test ------------")
        fixtPlaylist.scanOrder()
            .apply { println(this.toString()) }
    }

    @After
    fun tearDown() {
        println("------- end test ------------")
    }

    private fun assertScanOrder(actual: PlaylistDomain) {
        var lastorder = -1L
        actual.items.forEachIndexed { i, item ->
            if (lastorder > -1 && lastorder >= item.order) {
                (max(
                    i - 2,
                    0
                )..i + 2).forEach { println("order $it = ${fixtPlaylist.items[it].order}") }
                assertTrue("items $i order incorrect", false)
            }
            lastorder = item.order
        }
    }

    @Test
    fun `moveItem from start to end - jump fwd over current`() {
        val initialCurrentIndex = 2
        val fromPosition = 0
        val toPosition = fixtPlaylist.items.size - 1
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex)
        val fixMovedItem = fixtPlaylist.items[fromPosition]

        println("move: $fromPosition -> $toPosition")

        val actual = sut.moveItem(fixWithIndex, fromPosition, toPosition)

        assertEquals(initialCurrentIndex - 1, actual.currentIndex)
        assertEquals(fixMovedItem.id, actual.items[toPosition].id)
        assertEquals(actual.items[toPosition - 1].order + 1000, actual.items[toPosition].order)
        assertScanOrder(actual)
        actual.scanOrder().apply { println(this.toString()) }
    }

    @Test
    fun `moveItem from middle to start  - jump back over current`() {
        val initialCurrentIndex = 1
        val fromPosition = 2
        val toPosition = 0
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex)
        val fixMovedItem = fixtPlaylist.items[fromPosition]

        println("move: $fromPosition -> $toPosition")
        val actual = sut.moveItem(fixWithIndex, fromPosition, toPosition)

        assertEquals(initialCurrentIndex + 1, actual.currentIndex)
        assertEquals(fixMovedItem.id, actual.items[toPosition].id)
        assertEquals(actual.items[1].order - 1000, actual.items[toPosition].order)
        assertScanOrder(actual)
        actual.scanOrder().apply { println(this.toString()) }
    }

    @Test
    fun `moveItem from middle to middle fwd - ahead of current`() {
        val initialCurrentIndex = 1
        val fromPosition = 3
        val toPosition = 5
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex)
        val fixMovedItem = fixtPlaylist.items[fromPosition]

        println("move: $fromPosition -> $toPosition")
        val actual = sut.moveItem(fixWithIndex, fromPosition, toPosition)

        assertEquals(initialCurrentIndex, actual.currentIndex)
        assertEquals(fixMovedItem.id, actual.items[toPosition].id)
        assertEquals(
            (actual.items[toPosition - 1].order + actual.items[toPosition + 1].order) / 2,
            actual.items[toPosition].order
        )
        assertScanOrder(actual)
        actual.scanOrder().apply { println(this.toString()) }
    }

    @Test
    fun `moveItem from middle to end - ahead of current`() {
        val initialCurrentIndex = 1
        val fromPosition = 2
        val toPosition = fixtPlaylist.items.size - 1
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex)
        val fixMovedItem = fixtPlaylist.items[fromPosition]

        println("move: $fromPosition -> $toPosition")
        val actual = sut.moveItem(fixWithIndex, fromPosition, toPosition)

        assertEquals(initialCurrentIndex, actual.currentIndex)
        assertEquals(fixMovedItem.id, actual.items[toPosition].id)
        assertEquals(actual.items[toPosition - 1].order + 1000, actual.items[toPosition].order)
        assertScanOrder(actual)
        actual.scanOrder().apply { println(this.toString()) }
    }

    @Test
    fun `moveItem from middle to start - behind current`() {
        val initialCurrentIndex = 8
        val fromPosition = 2
        val toPosition = 0
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex)
        val fixMovedItem = fixtPlaylist.items[fromPosition]

        println("move: $fromPosition -> $toPosition")
        val actual = sut.moveItem(fixWithIndex, fromPosition, toPosition)

        assertEquals(initialCurrentIndex, actual.currentIndex)
        assertEquals(fixMovedItem.id, actual.items[toPosition].id)
        assertEquals(actual.items[1].order - 1000, actual.items[toPosition].order)
        assertScanOrder(actual)
        actual.scanOrder().apply { println(this.toString()) }
    }

    @Test
    fun `move current item changes index`() {
        val initialCurrentIndex = 2
        val fromPosition = 2
        val toPosition = 8
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex)
        val fixMovedItem = fixtPlaylist.items[fromPosition]

        println("move: $fromPosition -> $toPosition")
        val actual = sut.moveItem(fixWithIndex, fromPosition, toPosition)

        assertEquals(toPosition, actual.currentIndex)
        assertEquals(fixMovedItem.id, actual.items[toPosition].id)
        assertEquals((actual.items[toPosition - 1].order + actual.items[toPosition + 1].order) / 2, actual.items[toPosition].order)
        assertScanOrder(actual)
        actual.scanOrder().apply { println(this.toString()) }
    }

    @Test
    fun `goto previous single`() {
        val initialCurrentIndex = 1
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex, mode = SINGLE)

        val actual = sut.gotoPreviousItem(fixWithIndex)

        assertEquals(initialCurrentIndex - 1, actual.currentIndex)
    }

    @Test
    fun `goto previous single begin`() {
        val fixWithIndex = fixtPlaylist.copy(currentIndex = 0, mode = SINGLE)

        val actual = sut.gotoPreviousItem(fixWithIndex)

        assertEquals(-1, actual.currentIndex)
    }

    @Test
    fun `goto previous loop begin`() {
        val fixWithIndex = fixtPlaylist.copy(currentIndex = 0, mode = LOOP)

        val actual = sut.gotoPreviousItem(fixWithIndex)

        assertEquals(fixtPlaylist.items.size - 1, actual.currentIndex)
    }

    @Test
    fun `goto Next Item single`() {
        val fixWithIndex = fixtPlaylist.copy(currentIndex = 1, mode = SINGLE)

        val actual = sut.gotoNextItem(fixWithIndex)

        assertEquals(2, actual.currentIndex)
    }

    @Test
    fun `goto Next Item single end`() {
        val fixWithIndex =
            fixtPlaylist.copy(currentIndex = fixtPlaylist.items.size - 1, mode = SINGLE)

        val actual = sut.gotoNextItem(fixWithIndex)

        assertEquals(fixtPlaylist.items.size, actual.currentIndex)
    }

    @Test
    fun `goto Next Item loop end`() {
        val initialCurrentIndex = fixtPlaylist.items.size - 1
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex, mode = LOOP)

        val actual = sut.gotoNextItem(fixWithIndex)

        assertEquals(0, actual.currentIndex)
    }

    @Test
    fun playItem() {
        val fixWithIndex = fixtPlaylist.copy(currentIndex = 0, mode = LOOP)
        val selectedItem = fixWithIndex.items[2]

        val actual = sut.playItem(fixWithIndex, selectedItem)

        assertEquals(2, actual.currentIndex)
    }

    @Test
    fun `delete current`() {
        val fixtCurrentIndex = 1
        val fixWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        val deleteItem = fixWithIndex.items[fixtCurrentIndex]

        val actual = sut.delete(fixWithIndex, deleteItem)

        assertEquals(10, actual.items.size)
        assertEquals(fixtCurrentIndex, actual.currentIndex)
    }

    @Test
    fun `delete item before current`() {
        val fixtCurrentIndex = 2
        val fixWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        val deleteItem = fixWithIndex.items[1]

        val actual = sut.delete(fixWithIndex, deleteItem)

        assertEquals(10, actual.items.size)
        assertEquals(fixtCurrentIndex - 1, actual.currentIndex)
    }

    @Test
    fun `delete last current`() {
        val fixtCurrentIndex = fixtPlaylist.items.size - 1
        val fixWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        val deleteItem = fixWithIndex.items[fixtCurrentIndex]

        val actual = sut.delete(fixWithIndex, deleteItem)

        assertEquals(10, actual.items.size)
        assertEquals(fixtCurrentIndex - 1, actual.currentIndex)
    }

}