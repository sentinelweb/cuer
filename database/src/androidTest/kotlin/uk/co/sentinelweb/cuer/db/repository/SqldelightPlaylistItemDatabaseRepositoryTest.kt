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
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FLAT
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.db.util.DataCreation
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SqldelightPlaylistItemDatabaseRepositoryTest : KoinTest {
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
    val itemMapper: PlaylistItemMapper by inject()

    val sut: PlaylistItemDatabaseRepository by inject()
    val mediaRepo: MediaDatabaseRepository by inject()

    lateinit var dataCreation: DataCreation

    @Before
    fun before() {
        Database.Schema.create(get())
        dataCreation = DataCreation(database, fixture)
    }

    @Test
    fun createFlat() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val toCreate =
            fixture<PlaylistItemDomain>()
                .run { copy(playlistId = playlistEntity.id, media = media.copy(id = null)) }
        sut.updates.test {
            val actual = sut.save(toCreate, flat = true, emit = true)
            assertTrue(actual.isSuccessful)
            assertNotNull(actual.data)
            val expectedLoaded = sut.load(actual.data!!.id!!)
            assertNotNull(expectedLoaded.data)
            assertEquals(expectedLoaded.data!!, actual.data!!)

            assertEquals(FLAT to expectedLoaded.data!!, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun updateFlat() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val toCreate =
            fixture<PlaylistItemDomain>()
                .run { copy(playlistId = playlistEntity.id, media = media.copy(id = null)) }
        val created = sut.save(toCreate, flat = true, emit = true)
        assertTrue(created.isSuccessful)
        assertNotNull(created.data)

        val toUpdate = fixture<PlaylistItemDomain>()
            .run {
                copy(
                    id = created.data!!.id!!,
                    playlistId = playlistEntity.id,
                    media = created.data!!.media
                )
            }

        sut.updates.test {
            val updated = sut.save(toUpdate, flat = true, emit = true)
            assertTrue(updated.isSuccessful)
            assertNotNull(updated.data)
            val expectedLoaded = sut.load(created.data!!.id!!)
            assertNotNull(expectedLoaded.data)
            assertEquals(expectedLoaded.data!!, updated.data!!)

            assertEquals(FLAT to expectedLoaded.data!!, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun saveFull() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val toCreate =
            fixture<PlaylistItemDomain>()
                .run { copy(playlistId = playlistEntity.id, media = media.copy(id = null)) }
        val created = sut.save(toCreate, flat = true, emit = true)
        assertTrue(created.isSuccessful)
        assertNotNull(created.data)

        val toUpdate = fixture<PlaylistItemDomain>()
            .run {
                copy(
                    id = created.data!!.id!!,
                    playlistId = playlistEntity.id,
                    media = media.copy(created.data!!.media.id)// should update the media in the db
                )
            }

        sut.updates.test {
            val updated = sut.save(toUpdate, flat = false, emit = true)
            assertTrue(updated.isSuccessful)
            assertNotNull(updated.data)
            val expectedLoaded = sut.load(created.data!!.id!!)
            assertNotNull(expectedLoaded.data)
            assertEquals(expectedLoaded.data!!, updated.data!!)

            assertEquals(FULL to expectedLoaded.data!!, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun saveList() {
    }

    @Test
    fun loadFlat() = runTest {
        val (_, itemEntity) = dataCreation.createPlaylistAndItem()
        val expectedDomain = itemMapper.map(itemEntity, mediaRepo.load(itemEntity.media_id).data!!)
        val actual = sut.load(itemEntity.id)
        assertTrue(actual.isSuccessful)
        assertEquals(expectedDomain, actual.data)
    }

    @Test
    fun loadDoesntExist() = runTest {
        val actual = sut.load(42)
        assertFalse(actual.isSuccessful)
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