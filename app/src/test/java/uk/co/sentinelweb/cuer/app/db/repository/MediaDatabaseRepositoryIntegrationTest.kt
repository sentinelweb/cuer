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
import uk.co.sentinelweb.cuer.app.db.mapper.ChannelMapper
import uk.co.sentinelweb.cuer.app.db.mapper.ImageMapper
import uk.co.sentinelweb.cuer.app.db.mapper.MediaMapper
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository.IdListFilter
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextTestProvider
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain

/**
 * Integration test for MediaDatabaseRepository
 */
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class MediaDatabaseRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val coCxtProvider: CoroutineContextProvider =
        CoroutineContextTestProvider(testCoroutineDispatcher)

    @Fixture
    private lateinit var fixtMedia: MediaDomain

    @Fixture
    private lateinit var fixtChannel: ChannelDomain

    private val imageMapper = ImageMapper()
    private val channelMapper = ChannelMapper(imageMapper)
    private val mediaMapper = MediaMapper(imageMapper, channelMapper)

    private lateinit var sut: MediaDatabaseRepository

    @get:Rule
    var instantTaskExecutor = InstantTaskExecutorRule()

    private val log = SystemLogWrapper()

    @Before
    fun setUp() {
        FixtureAnnotations.initFixtures(this)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries()
                .build()

        sut = MediaDatabaseRepository(
            mediaDao = database.mediaDao(),
            mediaMapper = mediaMapper,
            channelDao = database.channelDao(),
            channelMapper = channelMapper,
            log = log,
            coProvider = coCxtProvider,
            database = database
        )

        fixtMedia = fixtMedia.copy(
            id = null,
            channelData = fixtMedia.channelData.copy(id = null)
        )
    }

    @After
    fun tearDown() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun save() {
        runBlocking {

            val result = sut.save(fixtMedia)

            assertTrue(result.isSuccessful)
            val actual = result.data!!
            assertNotNull(actual.id)
            assertEquals(result.data, sut.load(actual.id!!.toLong()).data)
            verifyMediaFields(fixtMedia, actual)
        }
    }

    @Test
    fun saveList() {
        runBlocking {
            val domain1 = fixtMedia.copy(
                title = "new",
                platformId = "xxxxxxx"//,
                //channelData = domain.channelData.copy(remoteId = "yyyyyyyy") // commented out tests duplicate channel
            )
            val domains = listOf(fixtMedia, domain1)

            val result = sut.save(domains)
            assertTrue(result.isSuccessful)
            val actual = result.data!!
            assertNotNull(actual[0].id)
            assertNotNull(actual[1].id)
            assertNotEquals(actual[0].id, actual[1].id)

            assertEquals(
                result.data,
                sut.loadList(IdListFilter(actual.map { it.id!!.toLong() })).data
            )
            domains.forEachIndexed { i, m -> verifyMediaFields(m, actual[i]) }
        }
    }

    @Test
    fun load() {
        // todo tested in save
    }

    @Test
    fun loadList() {
        // todo tested in saveList
    }

    @Test
    fun delete() {
        runBlocking {
            val resultSave = sut.save(fixtMedia)
            assertTrue(resultSave.isSuccessful)
            val resultCount = sut.count(IdListFilter(listOf(resultSave.data!!.id!!.toLong())))
            assertTrue(resultCount.isSuccessful && resultCount.data!! == 1)

            val resultDelete = sut.delete(resultSave.data!!)
            assertTrue(resultDelete.isSuccessful)

            val resultLoad = sut.load(resultSave.data!!.id!!.toLong())
            assertFalse(resultLoad.isSuccessful)
        }
    }

    @Test
    fun deleteAll() {
        runBlocking {
            val resultSave = sut.save(fixtMedia)
            assertTrue(resultSave.isSuccessful)

            val resultDeleteAll = sut.deleteAll()
            assertTrue(resultDeleteAll.isSuccessful)

            val resultCount = sut.count(IdListFilter(listOf(resultSave.data!!.id!!.toLong())))
            assertTrue(resultCount.isSuccessful && resultCount.data!! == 0)
        }
    }

    @Test
    fun count() {
        runBlocking {
            val resultSave = sut.save(fixtMedia)
            assertTrue(resultSave.isSuccessful)

            // test
            val resultCount = sut.count(IdListFilter(listOf(resultSave.data!!.id!!.toLong())))
            // verify
            assertTrue(resultCount.isSuccessful && resultCount.data!! == 1)

            val resultSave1 = sut.save(fixtMedia.copy(platformId = "x"))
            assertTrue(resultSave1.isSuccessful)

            // test
            val resultCount1 = sut.count(IdListFilter(listOf(resultSave.data!!.id!!.toLong())))
            // verify
            assertTrue(resultCount1.isSuccessful && resultCount1.data!! == 2)
        }
    }

    @Test
    fun loadChannel() {
        runBlocking {
            val insertId =
                database.channelDao().insert(channelMapper.map(fixtChannel.copy(id = null)))

            val result = sut.loadChannel(insertId)
            assertTrue(result.isSuccessful)

            assertEquals(fixtChannel.copy(id = insertId), result.data)
        }
    }

    private fun verifyMediaFields(expected: MediaDomain, actual: MediaDomain) {
        assertEquals(expected.title, actual.title)
        assertEquals(expected.description, actual.description)
        assertEquals(expected.dateLastPlayed, actual.dateLastPlayed)
        assertEquals(expected.duration, actual.duration)
        assertEquals(expected.platformId, actual.platformId)
        assertEquals(expected.image, actual.image)
        assertEquals(expected.thumbNail, actual.thumbNail)
        assertEquals(expected.mediaType, actual.mediaType)
        assertEquals(expected.platform, actual.platform)
        assertEquals(expected.positon, actual.positon)
        assertEquals(expected.published, actual.published)
        assertEquals(expected.starred, actual.starred)
        assertEquals(expected.watched, actual.watched)
        assertEquals(expected.channelData.country, actual.channelData.country)
        assertEquals(expected.channelData.customUrl, actual.channelData.customUrl)
        assertEquals(expected.channelData.description, actual.channelData.description)
        assertEquals(expected.channelData.image, actual.channelData.image)
        assertEquals(expected.channelData.thumbNail, actual.channelData.thumbNail)
        assertEquals(expected.channelData.platform, actual.channelData.platform)
        assertEquals(expected.channelData.platformId, actual.channelData.platformId)
        assertEquals(expected.channelData.starred, actual.channelData.starred)
        assertEquals(expected.channelData.title, actual.channelData.title)
        assertEquals(expected.channelData.published, actual.channelData.published)
    }
}