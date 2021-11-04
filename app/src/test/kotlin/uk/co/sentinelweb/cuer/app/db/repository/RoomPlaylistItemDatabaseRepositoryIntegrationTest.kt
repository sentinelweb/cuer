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
class RoomPlaylistItemDatabaseRepositoryIntegrationTest {
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
    private lateinit var channelReopsitory: RoomChannelDatabaseRepository

    private lateinit var sut: RoomPlaylistItemDatabaseRepository

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
        channelReopsitory = RoomChannelDatabaseRepository(
            channelDao = database.channelDao(),
            channelMapper = channelMapper,
            coProvider = coCxtProvider,
            log = systemLogWrapper,
            database = database
        )
        roomMediaRepo = RoomMediaDatabaseRepository(
            mediaDao = database.mediaDao(),
            channelDatabaseRepository = channelReopsitory,
            log = systemLogWrapper,
            database = database,
            mediaMapper = mediaMapper,
            coProvider = coCxtProvider,
            mediaUpdateMapper = mediaUpdateMapper
        )
        sut = RoomPlaylistItemDatabaseRepository(
            playlistItemDao = database.playlistItemDao(),
            playlistItemMapper = playlistItemMapper,
            roomMediaRepository = roomMediaRepo,
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