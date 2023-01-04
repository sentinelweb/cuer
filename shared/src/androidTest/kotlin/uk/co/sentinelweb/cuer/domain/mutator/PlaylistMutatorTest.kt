package uk.co.sentinelweb.cuer.domain.mutator

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.JFixture
import com.flextrade.jfixture.annotations.Fixture
import com.google.common.truth.Truth.assertThat
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
import uk.co.sentinelweb.cuer.tools.ext.build
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
                    order = 10000L + (idx * 1000L),
                    playlistId = fixtPlaylist.id

                )
            }
        )
    }

    @After
    fun tearDown() {
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
    fun moveItem_from_start_to_end__jump_fwd_over_current() {
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
    fun moveItem_from_middle_to_start___jump_back_over_current() {
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
    fun moveItem_from_middle_to_middle_fwd__ahead_of_current() {
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
    fun moveItem_from_middle_to_end__ahead_of_current() {
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
    fun moveItem_from_middle_to_start__behind_current() {
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
    fun move_current_item_changes_index() {
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
    fun goto_previous_single() {
        val initialCurrentIndex = 1
        val fixWithIndex = fixtPlaylist.copy(currentIndex = initialCurrentIndex, mode = SINGLE)

        val actual = sut.gotoPreviousItem(fixWithIndex)

        assertEquals(initialCurrentIndex - 1, actual.currentIndex)
    }

    @Test
    fun goto_previous_single_begin() {
        val fixWithIndex = fixtPlaylist.copy(currentIndex = 0, mode = SINGLE)

        val actual = sut.gotoPreviousItem(fixWithIndex)

        assertEquals(-1, actual.currentIndex)
    }

    @Test
    fun goto_previous_loop_begin() {
        val fixWithIndex = fixtPlaylist.copy(currentIndex = 0, mode = LOOP)

        val actual = sut.gotoPreviousItem(fixWithIndex)

        assertEquals(fixtPlaylist.items.size - 1, actual.currentIndex)
    }

    @Test
    fun goto_Next_Item_single() {
        val fixWithIndex = fixtPlaylist.copy(currentIndex = 1, mode = SINGLE)

        val actual = sut.gotoNextItem(fixWithIndex)

        assertEquals(2, actual.currentIndex)
    }

    @Test
    fun goto_Next_Item_single_end() {
        val fixWithIndex =
            fixtPlaylist.copy(currentIndex = fixtPlaylist.items.size - 1, mode = SINGLE)

        val actual = sut.gotoNextItem(fixWithIndex)

        assertEquals(fixtPlaylist.items.size, actual.currentIndex)
    }

    @Test
    fun goto_Next_Item_loop_end() {
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
    fun delete_current() {
        val fixtCurrentIndex = 1
        val fixWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        val deleteItem = fixWithIndex.items[fixtCurrentIndex]

        val actual = sut.delete(fixWithIndex, deleteItem)

        assertEquals(10, actual.items.size)
        assertEquals(fixtCurrentIndex, actual.currentIndex)
    }

    @Test
    fun delete_item_before_current() {
        val fixtCurrentIndex = 2
        val fixWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        val deleteItem = fixWithIndex.items[1]

        val actual = sut.delete(fixWithIndex, deleteItem)

        assertEquals(10, actual.items.size)
        assertEquals(fixtCurrentIndex - 1, actual.currentIndex)
    }

    @Test
    fun delete_last_current() {
        val fixtCurrentIndex = fixtPlaylist.items.size - 1
        val fixWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        val deleteItem = fixWithIndex.items[fixtCurrentIndex]

        val actual = sut.delete(fixWithIndex, deleteItem)

        assertEquals(10, actual.items.size)
        assertEquals(fixtCurrentIndex - 1, actual.currentIndex)
    }

    @Test
    fun addOrReplaceItem__same_order__no_index_change() {
        val fixtReplaceItemIndex = 2
        val fixtCurrentIndex = 3
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        val fixtChangedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylist.items.get(fixtReplaceItemIndex).id,
            playlistId = fixtPlaylist.id,
            order = fixtPlaylist.items.get(fixtReplaceItemIndex).order
        )

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtChangedItem)

        assertThat(actual.items.get(fixtReplaceItemIndex)).isEqualTo(fixtChangedItem)
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex)
    }

    @Test
    fun addOrReplaceItem__behind_current_item__moved_forward__past_current_Index__decrement_index() {
        val changeItemIndex = 2
        val fixtCurrentIndex = 3
        val targetIndexPreMove = 4
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtChangedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylist.items.get(changeItemIndex).id,
            playlistId = fixtPlaylist.id,
            order = fixtPlaylist.items.get(targetIndexPreMove).order - 100
        )

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtChangedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.get(targetIndexPreMove - 1)).isEqualTo(fixtChangedItem)
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex - 1)
    }

    @Test
    fun addOrReplaceItem__is_current_item__moved_forward__moves_index_with_it() {
        val changeItemIndex = 3
        val fixtCurrentIndex = 3
        val targetIndexPreMove = 6
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtChangedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylist.items.get(changeItemIndex).id,
            playlistId = fixtPlaylist.id,
            order = fixtPlaylist.items.get(targetIndexPreMove).order - 100
        )

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtChangedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.get(targetIndexPreMove - 1)).isEqualTo(fixtChangedItem)
        assertThat(actual.currentIndex).isEqualTo(targetIndexPreMove - 1)
    }

    @Test
    fun addOrReplaceItem__is_current_item__moved_backward__moves_index_with_it() {
        val changeItemIndex = 3
        val fixtCurrentIndex = 3
        val targetIndexPreMove = 1
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtChangedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylist.items.get(changeItemIndex).id,
            playlistId = fixtPlaylist.id,
            order = fixtPlaylist.items.get(targetIndexPreMove).order - 100
        )

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtChangedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.get(targetIndexPreMove)).isEqualTo(fixtChangedItem)
        assertThat(actual.currentIndex).isEqualTo(targetIndexPreMove)
    }

    @Test
    fun addOrReplaceItem__behind_current_item__moved_forward__to_before_current_Index__same_index() {
        val changeItemIndex = 1
        val fixtCurrentIndex = 3
        val targetIndexPreMove = 3
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtChangedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylist.items.get(changeItemIndex).id,
            playlistId = fixtPlaylist.id,
            order = fixtPlaylist.items.get(targetIndexPreMove).order - 100
        )

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtChangedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.get(targetIndexPreMove - 1)).isEqualTo(fixtChangedItem)
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex)
    }

    @Test
    fun addOrReplaceItem__ahead_of_current_item__moved_backward__to_before_current_Index__inc_index() {
        val changeItemIndex = 6
        val fixtCurrentIndex = 3
        val targetIndexPreMove = 3
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtChangedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylist.items.get(changeItemIndex).id,
            playlistId = fixtPlaylist.id,
            order = fixtPlaylist.items.get(targetIndexPreMove).order - 100
        )

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtChangedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.get(targetIndexPreMove)).isEqualTo(fixtChangedItem)
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex + 1)
    }

    @Test
    fun addOrReplaceItem__ahead_of_current_item__moved_backward__to_ahead_current_Index__same_index() {
        val changeItemIndex = 6
        val fixtCurrentIndex = 3
        val targetIndexPreMove = 4
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtChangedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylist.items.get(changeItemIndex).id,
            playlistId = fixtPlaylist.id,
            order = fixtPlaylist.items.get(targetIndexPreMove).order - 100
        )

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtChangedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.get(targetIndexPreMove)).isEqualTo(fixtChangedItem)
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex)
    }

    @Test
    fun addOrReplaceItem__add_new_item_before_index__inc_index() {
        val fixtCurrentIndex = 3
        val targetIndexPreMove = 3
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtChangedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixture.build<Long>(),
            playlistId = fixtPlaylist.id,
            order = fixtPlaylist.items.get(targetIndexPreMove).order - 100
        )

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtChangedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.get(targetIndexPreMove)).isEqualTo(fixtChangedItem)
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex + 1)
    }

    @Test
    fun addOrReplaceItem__add_new_item_after_index__inc_index() {
        val fixtCurrentIndex = 3
        val targetIndexPreMove = 4
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtChangedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixture.build<Long>(),
            playlistId = fixtPlaylist.id,
            order = fixtPlaylist.items.get(targetIndexPreMove).order - 100
        )

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtChangedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.get(targetIndexPreMove)).isEqualTo(fixtChangedItem)
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex)
    }

    @Test
    fun addOrReplaceItem__same_item_replaced__should_not_change() {
        val fixtCurrentIndex = 3
        val targetIndexReplace = 4
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtSameItem = fixtPlaylist.items.get(targetIndexReplace)

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtSameItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items).isEqualTo(fixtPlaylist.items)
        assertThat(actual.items.get(targetIndexReplace)).isEqualTo(fixtSameItem)
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex)
    }

    @Test(expected = IllegalArgumentException::class)
    fun addOrReplaceItem__not_on_playlist_throws_exception() {
        val fixtCurrentIndex = 3
        val targetIndexReplace = 4
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtNotOnPlaylistItem = fixtPlaylist.items.get(targetIndexReplace).copy(
            id = fixture.build(),
            playlistId = fixtPlaylist.id?.let { it + 100 }
        )

        val actual = sut.addOrReplaceItem(fixtPlaylistWithIndex, fixtNotOnPlaylistItem)
        // throws exception
    }

    @Test
    fun remove__is_before_currentIndex() {
        val fixtCurrentIndex = 3
        val removedIndex = 2
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtRemovedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylistWithIndex.items.get(removedIndex).id,
            playlistId = fixtPlaylist.id
        )

        val actual = sut.remove(fixtPlaylistWithIndex, fixtRemovedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.size).isEqualTo(fixtPlaylistWithIndex.items.size - 1)
        assertThat(actual.items.find { fixtRemovedItem.id == it.id }).isNull()
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex - 1)
    }

    @Test
    fun remove__is_after_afterIndex() {
        val fixtCurrentIndex = 3
        val removedIndex = 4
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtRemovedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylistWithIndex.items.get(removedIndex).id,
            playlistId = fixtPlaylist.id
        )

        val actual = sut.remove(fixtPlaylistWithIndex, fixtRemovedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.size).isEqualTo(fixtPlaylistWithIndex.items.size - 1)
        assertThat(actual.items.find { fixtRemovedItem.id == it.id }).isNull()
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex)
    }

    @Test
    fun remove__is_not_in_list__returns_same_list() {
        val fixtCurrentIndex = 3
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtRemovedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixture.build(),
            playlistId = fixtPlaylist.id
        )

        val actual = sut.remove(fixtPlaylistWithIndex, fixtRemovedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.size).isEqualTo(fixtPlaylistWithIndex.items.size)
        assertThat(actual.items.find { fixtRemovedItem.id == it.id }).isNull()
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex)
    }

    @Test
    fun remove__is_not_same_playlist_id_after_current() {
        val fixtCurrentIndex = 3
        val removedIndex = 4
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtRemovedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylistWithIndex.items.get(removedIndex).id,
            playlistId = fixtPlaylistWithIndex.id!! + 1
        )

        val actual = sut.remove(fixtPlaylistWithIndex, fixtRemovedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.size).isEqualTo(fixtPlaylistWithIndex.items.size - 1)
        assertThat(actual).isNotEqualTo(fixtPlaylistWithIndex)
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex)
        assertThat(actual.items.find { it.id == fixtRemovedItem.id }).isNull()
    }

    @Test
    fun remove__is_not_same_playlist_id_before_current() {
        val fixtCurrentIndex = 3
        val removedIndex = 2
        val fixtPlaylistWithIndex = fixtPlaylist.copy(currentIndex = fixtCurrentIndex)
        fixtPlaylistWithIndex.scanOrder().apply { println(this.toString()) }
        val fixtRemovedItem = fixture.build<PlaylistItemDomain>().copy(
            id = fixtPlaylistWithIndex.items.get(removedIndex).id,
            playlistId = fixtPlaylistWithIndex.id!! + 1
        )

        val actual = sut.remove(fixtPlaylistWithIndex, fixtRemovedItem)
        actual.scanOrder().apply { println(this.toString()) }

        assertThat(actual.items.size).isEqualTo(fixtPlaylistWithIndex.items.size - 1)
        assertThat(actual).isNotEqualTo(fixtPlaylistWithIndex)
        assertThat(actual.currentIndex).isEqualTo(fixtCurrentIndex - 1)
        assertThat(actual.items.find { it.id == fixtRemovedItem.id }).isNull()
    }
}