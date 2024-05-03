package uk.co.sentinelweb.cuer.app.orchestrator.memory

import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider
import uk.co.sentinelweb.cuer.tools.ext.generatePlaylist
import uk.co.sentinelweb.cuer.tools.ext.kotlinFixtureDefaultConfig
import uk.co.sentinelweb.cuer.tools.rule.CoroutineTestRule
import kotlin.test.assertEquals

class PlaylistMemoryRepositoryTest {
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

    lateinit var sut: PlaylistMemoryRepository

    @Before
    fun setUp() {
        sut = PlaylistMemoryRepository(
            coroutines,
            newItemsInteractor,
            recentItemsInteractor,
            localSearchInteractor,
            starredItemsInteractor,
            remoteSearchOrchestrator,
            unfinishedItemsInteractor,
            liveUpcomingItemsPlayistInteractor,
        )
    }

    @Test
    fun save_load_by_guid() = runTest {
        val fixtCurrentPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        sut.save(fixtCurrentPlaylist, fixtCurrentPlaylist.id!!.deepOptions())

        val actual = sut.load(fixtCurrentPlaylist.id!!.id, fixtCurrentPlaylist.id!!.deepOptions())

        assertEquals(fixtCurrentPlaylist, actual)
    }

    @Test
    fun save_load_by_playlist() = runTest {
        val fixtCurrentPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        sut.save(fixtCurrentPlaylist, fixtCurrentPlaylist.id!!.deepOptions())

        val actual = sut.load(fixtCurrentPlaylist, fixtCurrentPlaylist.id!!.deepOptions())

        assertEquals(fixtCurrentPlaylist, actual)
    }
}
