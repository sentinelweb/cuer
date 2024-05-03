package uk.co.sentinelweb.cuer.app.orchestrator.memory

import app.cash.turbine.test
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.MediaIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.DELETE
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider
import uk.co.sentinelweb.cuer.tools.ext.generatePlaylist
import uk.co.sentinelweb.cuer.tools.ext.kotlinFixtureDefaultConfig
import uk.co.sentinelweb.cuer.tools.rule.CoroutineTestRule

class PlaylistItemMemoryRepositoryTest {
    @get:Rule
    var rule = CoroutineTestRule()

    private val fixture = kotlinFixtureDefaultConfig

    private val coroutines: CoroutineContextProvider = TestCoroutineContextProvider(rule.dispatcher)
    private val newItemsInteractor: NewMediaPlayistInteractor = mockk(relaxed = true)
    private val recentItemsInteractor: RecentItemsPlayistInteractor = mockk(relaxed = true)
    private val localSearchInteractor: LocalSearchPlayistInteractor = mockk(relaxed = true)
    private val starredItemsInteractor: StarredItemsPlayistInteractor = mockk(relaxed = true)
    private val remoteSearchOrchestrator: YoutubeSearchPlayistInteractor = mockk(relaxed = true)
    private val unfinishedItemsInteractor: UnfinishedItemsPlayistInteractor = mockk(relaxed = true)
    private val liveUpcomingItemsPlayistInteractor: LiveUpcomingItemsPlayistInteractor = mockk(relaxed = true)

    lateinit var playlistMemoryRepository: PlaylistMemoryRepository
    lateinit var sut: PlaylistMemoryRepository.PlayListItemMemoryRepository

    @Before
    fun setUp() {
        playlistMemoryRepository = PlaylistMemoryRepository(
            coroutines,
            newItemsInteractor,
            recentItemsInteractor,
            localSearchInteractor,
            starredItemsInteractor,
            remoteSearchOrchestrator,
            unfinishedItemsInteractor,
            liveUpcomingItemsPlayistInteractor,
        )
        sut = playlistMemoryRepository.playlistItemMemoryRepository
    }

    @Test
    fun save_load_by_item() = runTest {
        val fixtCurrentPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtCurrentPlaylist, fixtCurrentPlaylist.id!!.deepOptions())
        val expected = fixtCurrentPlaylist.items[0]
        val actual = sut.load(expected, MEMORY.deepOptions())
        assertEquals(expected, actual)
    }

    @Test
    fun save_load_by_guid() = runTest {
        val fixtPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtPlaylist, fixtPlaylist.id!!.deepOptions())

        val expected = fixtPlaylist.items[0]
        val actual = sut.load(expected.id!!.id, MEMORY.deepOptions())
        assertEquals(expected, actual)
    }

    @Test
    fun save_loadlist_media_id_filter() = runTest {
        val fixtPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtPlaylist, fixtPlaylist.id!!.deepOptions())

        val filter = MediaIdListFilter(fixtPlaylist.items.map { it.media.id!!.id })
        val expected = fixtPlaylist.items
        val actual = sut.loadList(filter, MEMORY.deepOptions())
        assertEquals(expected, actual)
    }

    @Test
    fun save_updates_and_emits() = runTest {
        val fixtPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtPlaylist, fixtPlaylist.id!!.deepOptions())

        val expected = fixtPlaylist.items[0].copy(order = 1000)
        sut.updates.test {
            val actual = sut.save(expected, expected.id!!.deepOptions())
            val (operation, playlistItemDomain) = awaitItem()
            assertEquals(operation, FULL)
            assertEquals(playlistItemDomain, expected)
            assertEquals(actual, expected)
        }
    }

    @Test
    fun delete_deletes_and_emits(): Unit = runTest {
        val fixtPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtPlaylist, fixtPlaylist.id!!.deepOptions())

        val expected = fixtPlaylist.items[0].copy(order = 1000)
        sut.updates.test {
            val actual: Boolean = sut.delete(expected, expected.id!!.deepOptions())
            val (operation, playlistItemDomain) = awaitItem()
            assertEquals(operation, DELETE)
            assertEquals(playlistItemDomain, expected)
            assertTrue(actual)

            assertNull(sut.load(expected, expected.id!!.deepOptions()))
        }
    }
}
