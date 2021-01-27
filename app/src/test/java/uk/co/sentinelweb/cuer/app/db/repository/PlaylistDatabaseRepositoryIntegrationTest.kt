package uk.co.sentinelweb.cuer.app.db.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.annotations.Fixture
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.mapper.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextTestProvider
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

/**
 * Integration test for PlaylistDatabaseRepository
 */
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class PlaylistDatabaseRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val coCxtProvider: CoroutineContextProvider =
        CoroutineContextTestProvider(testCoroutineDispatcher)

    @Fixture
    private lateinit var playlist: PlaylistDomain

    @Fixture
    private lateinit var playlistItem: PlaylistItemDomain

    private val imageMapper = ImageMapper()
    private val channelMapper = ChannelMapper(imageMapper)
    private val mediaMapper = MediaMapper(imageMapper, channelMapper)
    private val playlistItemMapper = PlaylistItemMapper(mediaMapper)
    private lateinit var mediaRepo: MediaDatabaseRepository

    private lateinit var sut: PlaylistDatabaseRepository

    @get:Rule
    var instantTaskExecutor = InstantTaskExecutorRule()

    private val systemLogWrapper = SystemLogWrapper()

    @Before
    fun setUp() {
        FixtureAnnotations.initFixtures(this)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        mediaRepo = MediaDatabaseRepository(
            mediaDao = database.mediaDao(),
            channelDao = database.channelDao(),
            log = systemLogWrapper,
            database = database,
            channelMapper = channelMapper,
            mediaMapper = mediaMapper,
            coProvider = coCxtProvider
        )
        sut = PlaylistDatabaseRepository(
            playlistDao = database.playlistDao(),
            playlistMapper = PlaylistMapper(imageMapper, playlistItemMapper, channelMapper),
            playlistItemDao = database.playlistItemDao(),
            playlistItemMapper = playlistItemMapper,
            mediaDao = database.mediaDao(),
            log = systemLogWrapper,
            coProvider = coCxtProvider,
            database = database
        )
        runBlocking {
            mediaRepo.save(playlist.items.map { it.media.copy(channelData = it.media.channelData.copy(id = null)) })
        }
    }

    @After
    fun tearDown() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun save() {
        runBlocking {
            val domain = playlist.copy(id = null, items = playlist.items.map { it.copy(id = null) })
            val saved = sut.save(domain)
            assertTrue(saved.isSuccessful)
            assertEquals(saved.data, sut.load(saved.data!!.id!!.toLong()).data)
            systemLogWrapper.d(saved.data!!.toString())
            // retest to see that id is preserved
            val saved2 = sut.save(saved.data!!)
            assertEquals(saved.data, saved2.data)
        }
    }

    @Test
    fun saveList() {
        runBlocking {
            val domain = playlist.copy(id = null, items = playlist.items.map { it.copy(id = null) })
            val domain1 = domain.copy()
            val domains = listOf(domain, domain1)
            val saved = sut.save(domains)
            assertTrue(saved.isSuccessful)
            assertEquals(
                saved.data,
                sut.loadList(OrchestratorContract.IdListFilter(saved.data!!.map { it.id!!.toLong() })).data
            )
        }
    }

    @Test
    fun load() {
        // tested in save
    }

    @Test
    fun loadList() {
        // tested in saveList
    }

    @Test
    fun count() {
        runBlocking {
            val domain = playlist.copy(id = null, items = playlist.items.map { it.copy(id = null) })
            val actual = sut.save(domain)
            assertTrue(actual.isSuccessful)
            assertEquals(1, sut.count().data)
        }
    }

    @Test
    fun delete() {
        runBlocking {
            val domain = playlist.copy(id = null, items = playlist.items.map { it.copy(id = null) })

            val actual = sut.save(domain)
            assertTrue(actual.isSuccessful)

            sut.delete(actual.data!!)
            assertEquals(0, sut.count().data)
        }
    }

    @Test
    fun deleteAll() {
        runBlocking {
            val domain = playlist.copy(id = null, items = playlist.items.map { it.copy(id = null) })
            val actual = sut.save(domain)
            assertTrue(actual.isSuccessful)

            sut.deleteAll()
            assertEquals(0, sut.count().data)
        }
    }

    @Test
    fun savePlaylistItem() {
        runBlocking {
            val domain = playlistItem.copy(
                id = null,
                media = playlistItem.media.copy(
                    id = 2L,
                    channelData = playlistItem.media.channelData.copy(id = 3L, platformId = "x")
                )
            )

            val actual = sut.savePlaylistItem(domain)
            assertTrue(actual.isSuccessful)
            database.mediaDao().insert(mediaMapper.map(domain.media))
            database.channelDao().insert(channelMapper.map(domain.media.channelData))

            assertEquals(actual.data, sut.loadPlaylistItem(actual.data!!.id!!.toLong()).data)
        }
    }

    @Test
    fun deletePlaylistItem() {
        runBlocking {
            val domain = playlistItem.copy(
                id = null,
                media = playlistItem.media.copy(id = 2L)
            )

            val actual = sut.savePlaylistItem(domain)
            assertTrue(actual.isSuccessful)

            // test
            val actualDel = sut.delete(actual.data!!)
            assertTrue(actualDel.isSuccessful)

            // verify
            val actualLoad = sut.loadPlaylistItem(actual.data!!.id!!.toLong())
            assertFalse(actualLoad.isSuccessful)
        }
    }
}