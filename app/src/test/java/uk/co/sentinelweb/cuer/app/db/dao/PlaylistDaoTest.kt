package uk.co.sentinelweb.cuer.app.db.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.annotations.Fixture
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistEntity
import uk.co.sentinelweb.cuer.app.db.entity.PlaylistItemEntity

@RunWith(RobolectricTestRunner::class)
class PlaylistDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var playlistDao: PlaylistDao
    private lateinit var playlistItemDao: PlaylistItemDao

    @Fixture
    private lateinit var playlist: PlaylistEntity

    @Fixture
    private lateinit var playlistItem: PlaylistItemEntity

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

        playlistDao.insert(playlist)
        playlistItemDao.insert(playlistItem.copy(playlistId = playlist.id))
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getPlaylists() {
        val playlists = playlistDao.getPlaylists()

        assertEquals(1, playlists.size)
        assertEquals(1, playlists[0].items.size)
    }
}