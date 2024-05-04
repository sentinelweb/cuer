package uk.co.sentinelweb.cuer.app.orchestrator.memory

import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.Shared
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain
import uk.co.sentinelweb.cuer.tools.ext.generatePlaylist
import uk.co.sentinelweb.cuer.tools.ext.kotlinFixtureDefaultConfig
import uk.co.sentinelweb.cuer.tools.rule.CoroutineTestRule
import uk.co.sentinelweb.cuer.tools.rule.FlakyTestRule

class MediaMemoryRepositoryTest {
    @get:Rule
    var rule = CoroutineTestRule()

    @get:Rule
    var flakyTestRule = FlakyTestRule(5)

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
    lateinit var sut: PlaylistMemoryRepository.MediaMemoryRepository

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
        sut = playlistMemoryRepository.mediaMemoryRepository
    }

    @Test
    fun load_by_item() = runTest {
        val fixtCurrentPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtCurrentPlaylist, fixtCurrentPlaylist.id!!.deepOptions())
        val expected = fixtCurrentPlaylist.items[0].media
        val actual = sut.load(expected, MEMORY.deepOptions())
        assertEquals(expected, actual)
    }

    @Test
    fun load_by_guid() = runTest {
        val fixtCurrentPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtCurrentPlaylist, fixtCurrentPlaylist.id!!.deepOptions())
        val expected = fixtCurrentPlaylist.items[0].media
        val actual = sut.load(expected.id!!.id, MEMORY.deepOptions())
        assertEquals(expected, actual)
    }

    @Test
    fun save_nonNull_id() = runTest {
        val fixtCurrentPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtCurrentPlaylist, fixtCurrentPlaylist.id!!.deepOptions())
        val expected = fixtCurrentPlaylist.items[0].media.copy(url = "url-changed")
        val actual = sut.save(expected, MEMORY.deepOptions())
        assertEquals(expected, actual)
    }

    @Test
    fun save_null_id() = runTest {
        // note this function only works for the Shared playlist - which might not have ids assigned
        val fixtCurrentPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(id = Shared.id, source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtCurrentPlaylist, fixtCurrentPlaylist.id!!.deepOptions())
        val expected = fixtCurrentPlaylist.items[0].media.copy(id = null, url = "url-changed")

        // test
        val actual = sut.save(expected, MEMORY.deepOptions())

        //verify
        assertEquals(expected, actual)
        val replacedMediaItem = fixtCurrentPlaylist.items[0]
        val load = playlistMemoryRepository.playlistItemMemoryRepository
            .load(replacedMediaItem, replacedMediaItem.id!!.deepOptions())
        assertEquals(expected, load?.media)
    }

    @Test
    fun save_list() = runTest {
        val fixtCurrentPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtCurrentPlaylist, fixtCurrentPlaylist.id!!.deepOptions())
        val expected = fixtCurrentPlaylist.items.mapIndexed { i, item -> item.media.copy(url = "url-$i") }
        val actual = sut.save(expected, MEMORY.deepOptions())
        assertEquals(expected, actual)
    }

    @Test
    fun update_position() = runTest {
        val fixtCurrentPlaylist = generatePlaylist(fixture)
            .let { it.copy(id = it.id!!.copy(source = MEMORY)) }
            .let { pl -> pl.copy(items = pl.items.map { it.copy(playlistId = pl.id) }) }
        playlistMemoryRepository.save(fixtCurrentPlaylist, fixtCurrentPlaylist.id!!.deepOptions())

        val initial = fixtCurrentPlaylist.items[0].media
        val date = Clock.System.now()
        val update = MediaPositionUpdateDomain(
            initial.id!!,
            positon = 1000L,
            duration = 2000L,
            watched = true,
            dateLastPlayed = date
        )

        val actual = sut.update(update, MEMORY.deepOptions())
        val expected = initial.copy(positon = 1000L, duration = 2000L, watched = true, dateLastPlayed = date)
        assertEquals(expected, actual)
    }
}
