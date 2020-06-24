package uk.co.sentinelweb.cuer.app.util.helper

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.annotations.Fixture
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.LOOP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE

class PlaylistMutatorTest {

    @Fixture
    private lateinit var fixtPlaylist: PlaylistDomain

    private val sut = PlaylistMutator()

    @Before
    fun setUp() {
        FixtureAnnotations.initFixtures(this)
    }

    @Test
    fun `moveItem from behind to ahead of current`() {
        val initialCurrentIndex = 2
        val fromPosition = 0
        val toPosition = 2
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex)
        val fixMovedItem = fixtPlaylist.items[fromPosition]

        val actual = sut.moveItem(fixWithIndex, fromPosition, toPosition)

        assertEquals(initialCurrentIndex - 1, actual.currentIndex)
        assertEquals(fixMovedItem.id, actual.items[toPosition].id)
        assertEquals(actual.items[1].order + 1000, actual.items[toPosition].order)
    }

    @Test
    fun `moveItem from ahead to behind of current`() {
        val initialCurrentIndex = 0
        val fromPosition = 1
        val toPosition = 0
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex)
        val fixMovedItem = fixtPlaylist.items[fromPosition]

        val actual = sut.moveItem(fixWithIndex, fromPosition, toPosition)

        assertEquals(initialCurrentIndex + 1, actual.currentIndex)
        assertEquals(fixMovedItem.id, actual.items[0].id)
        assertEquals(actual.items[1].order - 1000, actual.items[toPosition].order)
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
}