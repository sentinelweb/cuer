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
import org.robolectric.annotation.Config
import uk.co.sentinelweb.cuer.app.CuerTestApp
import uk.co.sentinelweb.cuer.app.db.AppDatabase
import uk.co.sentinelweb.cuer.app.db.entity.ChannelEntity
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity

@RunWith(RobolectricTestRunner::class)
@Config(application = CuerTestApp::class)
class MediaDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var mediaDao: MediaDao
    private lateinit var channelDao: ChannelDao

    @Fixture
    private lateinit var mediaEntity: MediaEntity

    @Fixture
    private lateinit var channelEntity: ChannelEntity

    @get:Rule
    var instantTaskExecutor = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        FixtureAnnotations.initFixtures(this)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries()
                .build()
        mediaDao = database.mediaDao()
        channelDao = database.channelDao()

        runBlocking {
            mediaDao.insertAll(listOf(mediaEntity.copy(channelId = channelEntity.id)))
            channelDao.insertAll(listOf(channelEntity))
        }
    }

    @After
    fun tearDown() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun getAllMedia() {
        runBlocking {
            val medias = mediaDao.getAll()
            assertEquals(1, medias.size)
            assertEquals(medias[0].media.channelId, medias[0].channel.id)
            assertNotNull(medias[0].channel)
        }
    }
}