package uk.co.sentinelweb.cuer.db.repository

import app.cash.turbine.test
import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.app.db.repository.ChannelDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.db.mapper.PlaylistMapper
import uk.co.sentinelweb.cuer.db.util.DataCreation
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.db.util.resetIds
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqldelightPlaylistDatabaseRepositoryTest : KoinTest {
    private val fixture = kotlinFixture { nullabilityStrategy(NeverNullStrategy) }

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var dbIntegrationRule = DatabaseTestRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.ERROR)
        modules(
            listOf<Module>()
                .plus(dbIntegrationRule.modules)
                .plus(mainCoroutineRule.modules)
        )
    }

    val database: Database by inject()
    val playlistMapper: PlaylistMapper by inject()
    val itemMapper: PlaylistItemMapper by inject()

    val sut: PlaylistDatabaseRepository by inject()
    val mediaRepo: MediaDatabaseRepository by inject()
    val imageRepo: SqldelightImageDatabaseRepository by inject()
    val channelRepo: ChannelDatabaseRepository by inject()

    lateinit var dataCreation: DataCreation

    @Before
    fun before() {
        Database.Schema.create(get())
        dataCreation = DataCreation(database, fixture)
    }

    @Test
    fun createFlat() = runTest {
        val toCreate = fixture<PlaylistDomain>().resetIds()
        sut.updates.test {
            val actual = sut.save(toCreate, flat = true, emit = true)
            assertTrue(actual.isSuccessful)
            val expected = sut.load(actual.data!!.id!!, flat = true)
            assertEquals(expected.data!!, actual.data!!)
            assertEquals(0, actual.data!!.items.size)
            assertEquals(FLAT to expected.data!!, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun updateFlat() = runTest {
        val toCreate = fixture<PlaylistDomain>().resetIds()
        val created = sut.save(toCreate, flat = true, emit = false)
        assertTrue(created.isSuccessful)
        val toUpdate = fixture<PlaylistDomain>()
            .resetIds()
            .run { copy(id = created.data!!.id) }
        sut.updates.test {
            val actual = sut.save(toUpdate, flat = true, emit = true)
            assertTrue(actual.isSuccessful)
            val expected = sut.load(actual.data!!.id!!, flat = true)
            assertEquals(expected.data!!, actual.data!!)

            assertEquals(FLAT to expected.data!!, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun createFull() = runTest {
        val toCreate = fixture<PlaylistDomain>().resetIds()

        sut.updates.test {
            val actual = sut.save(toCreate, flat = false, emit = true)
            assertTrue(actual.isSuccessful)
            val expected = sut.load(actual.data!!.id!!, flat = false)
            assertTrue(expected.isSuccessful)
            assertEquals(expected.data!!, actual.data!!)
            assertEquals(toCreate.items.size, actual.data!!.items.size)
            assertEquals(FULL to expected.data!!, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun updateFull() = runTest {
        val toCreate = fixture<PlaylistDomain>().resetIds()
        val created = sut.save(toCreate, flat = false, emit = false)
        assertTrue(created.isSuccessful)
        val toUpdate = fixture<PlaylistDomain>()
            .resetIds()
            .run { copy(id = created.data!!.id) }

        sut.updates.test {
            val actual = sut.save(toUpdate, flat = false, emit = true)
            assertTrue(actual.isSuccessful)
            val expected = sut.load(actual.data!!.id!!, flat = false)
            assertTrue(expected.isSuccessful)
            assertEquals(expected.data!!, actual.data!!)

            assertEquals(FULL to expected.data!!, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun saveList() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>().map { it.resetIds() }
        sut.updates.test {
            val actual = sut.save(toCreate, flat = false, emit = true)
            assertTrue(actual.isSuccessful)
            actual.data!!.forEach {
                val expected = sut.load(it.id!!, flat = false)
                assertTrue(expected.isSuccessful)
                assertEquals(expected.data!!, it)

                assertEquals(FULL to expected.data!!, awaitItem())
            }
            expectNoEvents()
        }
    }

    @Test
    fun loadFlat() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val expectedDomain = playlistMapper.map(
            playlistEntity,
            listOf(),
            playlistEntity.channel_id?.let { channelRepo.load(it).data!! },
            playlistEntity.thumb_id?.let { imageRepo.loadEntity(it) },
            playlistEntity.image_id?.let { imageRepo.loadEntity(it) },
        )
        val actual = sut.load(playlistEntity.id, flat = true)
        assertTrue(actual.isSuccessful)
        assertEquals(expectedDomain, actual.data)
    }

    @Test
    fun loadFull() = runTest {
        val (playlistEntity, itemEntity) = dataCreation.createPlaylistAndItem()
        val expectedDomain = playlistMapper.map(
            playlistEntity,
            listOf(itemMapper.map(itemEntity, mediaRepo.load(itemEntity.media_id).data!!)),
            playlistEntity.channel_id?.let { channelRepo.load(it).data!! },
            playlistEntity.thumb_id?.let { imageRepo.loadEntity(it) },
            playlistEntity.image_id?.let { imageRepo.loadEntity(it) },
        )
        val actual = sut.load(playlistEntity.id, flat = false)
        assertTrue(actual.isSuccessful)
        assertEquals(expectedDomain, actual.data)
    }

    @Test
    fun loadList() {

    }

    @Test
    fun loadStatsList() {
    }

    @Test
    fun count() {
    }

    @Test
    fun delete() {
    }

    @Test
    fun deleteAll() {
    }

    @Test
    fun update() {
    }
}