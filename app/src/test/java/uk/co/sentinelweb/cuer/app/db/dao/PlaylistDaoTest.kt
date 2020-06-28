package uk.co.sentinelweb.cuer.app.db.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.annotations.Fixture
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistItemEntity

@RunWith(RobolectricTestRunner::class)
class PlaylistDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var playlistDao: PlaylistDao
    private lateinit var mediaDao: MediaDao
    private lateinit var channelDao: ChannelDao
    private lateinit var playlistItemDao: PlaylistItemDao

    @Fixture
    private lateinit var playlist: PlaylistEntity

    @Fixture
    private lateinit var playlistItem: PlaylistItemEntity

    @Fixture
    private lateinit var media: MediaEntity

    @Fixture
    private lateinit var channel: ChannelEntity

    @get:Rule
    var instantTaskExecutor = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        FixtureAnnotations.initFixtures(this)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries()
                .build()
        playlistDao = database.playlistDao()
        playlistItemDao = database.playlistItemDao()
        mediaDao = database.mediaDao()
        channelDao = database.channelDao()

        runBlocking {
            playlistDao.insert(playlist)
            playlistItemDao.insert(playlistItem.copy(playlistId = playlist.id, mediaId = media.id))
            mediaDao.insertAll(listOf(media.copy(channelId = channel.id)))
            channelDao.insertAll(listOf(channel))
        }
    }

    @After
    fun tearDown() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun getPlaylistsAndItems() {
        runBlocking {
            val actual = playlistDao.getAllPlaylistsWithItems()

            assertEquals(1, actual.size)
            assertEquals(1, actual[0].items.size)
            assertEquals(playlist.config, actual[0].playlist.config)
            val mediaActual = mediaDao.load(actual[0].items[0].mediaId)
            assertNotNull(mediaActual)
        }
    }

    @Test
    fun getPlaylists() {
        runBlocking {
            val actual = playlistDao.getAllPlaylists()

            assertEquals(1, actual.size)
            assertEquals(playlist.config, actual[0].config)
        }
    }
}