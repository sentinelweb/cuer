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
import org.robolectric.annotation.Config
import uk.co.sentinelweb.cuer.app.CuerTestApp
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.entity.update.MediaUpdateMapper
import uk.co.sentinelweb.cuer.app.db.mapper.*
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
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
@Config(application = CuerTestApp::class)
@ExperimentalCoroutinesApi
class RoomPlaylistDatabaseRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val coCxtProvider: CoroutineContextProvider =
        CoroutineContextTestProvider(testCoroutineDispatcher)

    @Fixture
    private lateinit var playlists: List<PlaylistDomain>

    @Fixture
    private lateinit var playlistItems: List<PlaylistItemDomain>

    private val imageMapper = ImageMapper()
    private val channelMapper = ChannelMapper(imageMapper)
    private val mediaMapper = MediaMapper(imageMapper, channelMapper)
    private val mediaUpdateMapper = MediaUpdateMapper(InstantTypeConverter())
    private val playlistItemMapper = PlaylistItemMapper(mediaMapper)
    private lateinit var roomMediaRepo: RoomMediaDatabaseRepository

    private lateinit var sut: RoomPlaylistDatabaseRepository

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
        roomMediaRepo = RoomMediaDatabaseRepository(
            mediaDao = database.mediaDao(),
            channelDao = database.channelDao(),
            log = systemLogWrapper,
            database = database,
            channelMapper = channelMapper,
            mediaMapper = mediaMapper,
            coProvider = coCxtProvider,
            mediaUpdateMapper = mediaUpdateMapper
        )
        sut = RoomPlaylistDatabaseRepository(
            playlistDao = database.playlistDao(),
            playlistMapper = PlaylistMapper(imageMapper, playlistItemMapper, channelMapper, systemLogWrapper),
            playlistItemDao = database.playlistItemDao(),
            channelDao = database.channelDao(),
            playlistItemMapper = playlistItemMapper,
            mediaRepository = roomMediaRepo,
            log = systemLogWrapper,
            coProvider = coCxtProvider,
            database = database
        )

    }

    private fun savePlaylistMedia() {
        runBlocking {
            roomMediaRepo.save(playlists.map { it.items.map { it.media.copy(channelData = it.media.channelData.copy(id = null)) } }
                .flatten())
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
            savePlaylistMedia()
            val playlist = playlists[0]
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
            savePlaylistMedia()
            val domains = playlists.map { it.copy(id = null, items = it.items.map { it.copy(id = null) }) }
            savePlaylistMedia()

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
            savePlaylistMedia()
            val playlist = playlists[0]
            val domain = playlist.copy(id = null, items = playlist.items.map { it.copy(id = null) })
            val actual = sut.save(domain)
            assertTrue(actual.isSuccessful)
            assertEquals(1, sut.count().data)
        }
    }

    @Test
    fun delete() {
        runBlocking {
            savePlaylistMedia()
            val playlist = playlists[0]
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
            savePlaylistMedia()
            val actual = sut.save(playlists)
            assertTrue(actual.isSuccessful)

            sut.deleteAll()
            assertEquals(0, sut.count().data)
        }
    }

    @Test
    fun savePlaylistItem() {
        runBlocking {
            val playlistItem = playlistItems[0]
            val domain = playlistItem.copy(
                id = null,
                media = playlistItem.media.copy(
                    id = null,
                    channelData = playlistItem.media.channelData.copy(id = null, platformId = "x")
                )
            )

            val saved = sut.savePlaylistItem(domain)
            assertTrue(saved.isSuccessful)

            val actual = sut.loadPlaylistItem(saved.data!!.id!!.toLong()).data
            assertEquals(saved.data, actual)
        }
    }

    @Test
    fun savePlaylistItems() {
        runBlocking {
            val domains = playlistItems.map {
                it.copy(
                    id = null,
                    media = it.media.copy(
                        id = null,
                        channelData = it.media.channelData.copy(id = null)
                    )
                )
            }

            val saved = sut.savePlaylistItems(domains)
            assertTrue(saved.isSuccessful)

            val filter = OrchestratorContract.IdListFilter(saved.data!!.map { it.id!!.toLong() })
            val actual = sut.loadPlaylistItems(filter).data
            assertEquals(saved.data, actual)
        }
    }

    @Test
    fun deletePlaylistItem() {
        val playlistItem = playlistItems[0]
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