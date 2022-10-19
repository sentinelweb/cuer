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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
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
        val created = sut.save(toCreate, flat = true, emit = false)
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
    fun saveList() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val toCreate =
            fixture<List<PlaylistItemDomain>>()
                .map { it.copy(playlistId = playlistEntity.id, media = it.media.copy(id = null)) }
        sut.updates.test {
            val created = sut.save(toCreate, flat = true, emit = true)
            assertTrue(created.isSuccessful)
            assertNotNull(created.data)
            val actual = created.data!!
            val expected = actual.map { sut.load(it.id!!).data!! }
            assertEquals(expected, actual)

            expected.forEach {
                assertEquals(FLAT to it, awaitItem())
            }
            expectNoEvents()
        }
    }

//    @Test
//    fun saveListOrderOverwrite() = runTest {
//        val playlistEntity = dataCreation.createPlaylist()
//        val toCreate =
//            fixture<List<PlaylistItemDomain>>()
//                .map { it.copy(playlistId = playlistEntity.id, media = it.media.copy(id = null), order = 5L) }
//        sut.updates.test {
//            val created = sut.save(toCreate, flat = true, emit = true)
//            val expected = playlistEntity.
//            assertTrue(created.isSuccessful)
//            assertEquals(expected, created.data!![0])
//            expectNoEvents()
//        }
//    }

    @Test
    fun loadListByIds() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val list =
            fixture<List<PlaylistItemDomain>>()
                .map { it.copy(playlistId = playlistEntity.id, media = it.media.copy(id = null)) }
        val saved = sut.save(list, true, false).data!!
        val actual = sut.loadList(OrchestratorContract.IdListFilter(listOf(1, 3))).data!!
        assertEquals(saved.filter { it.id == 1L || it.id == 3L }, actual)
    }

    @Test
    fun loadListByMediaIds() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val list =
            fixture<List<PlaylistItemDomain>>()
                .map { it.copy(playlistId = playlistEntity.id, media = it.media.copy(id = null)) }
        val saved = sut.save(list, true, false).data!!
        val actual = sut.loadList(OrchestratorContract.MediaIdListFilter(listOf(1, 3))).data!!
        assertEquals(saved.filter { it.media.id == 1L || it.media.id == 3L }, actual)
    }

    @Test
    fun loadListByPlatformIds() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val list = fixture<List<PlaylistItemDomain>>()
            .mapIndexed { i, item ->
                item.copy(
                    playlistId = playlistEntity.id,
                    media = item.media.copy(id = null, platformId = "platformId_$i")
                )
            }
        val saved = sut.save(list, true, false).data!!
        val platformIds = saved.filter { it.id == 1L || it.id == 3L }.map { it.media.platformId }
        val actual = sut.loadList(OrchestratorContract.PlatformIdListFilter(platformIds)).data!!
        assertEquals(platformIds.size, actual.size)
        assertEquals(saved.filter { platformIds.contains(it.media.platformId) }, actual)
    }

    @Test
    fun loadListByChannelId() = runTest {
        val (_, itemEntity1) = dataCreation.createPlaylistAndItem()
        val (_, itemEntity2) = dataCreation.createPlaylistAndItem()
        val (_, itemEntity3) = dataCreation.createPlaylistAndItem()
        val (_, _) = dataCreation.createPlaylistAndItem()

        val itemIds = listOf(itemEntity1.id, itemEntity2.id, itemEntity3.id) // 0, 1, 2
        val listItems = sut
            .loadList(OrchestratorContract.IdListFilter(ids = itemIds))
            .data!!
        val channelDomain = listItems[0].media.channelData
        val listItemsModified = listItems.map { it.copy(media = it.media.copy(channelData = channelDomain)) }
        val listItemsSaved = sut.save(listItemsModified).data!!
        assertEquals(listItemsModified, listItemsSaved)
        val actual =
            sut.loadList(OrchestratorContract.ChannelPlatformIdFilter(platformId = channelDomain.platformId!!)).data!!
        assertEquals(listItemsModified, actual)
    }

    // todo test NewMediaFilter RecentMediaFilter SearchFilter

    @Test
    fun delete() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val list =
            fixture<List<PlaylistItemDomain>>()
                .map { it.copy(playlistId = playlistEntity.id, media = it.media.copy(id = null)) }
        val saved = sut.save(list, true, false).data!!
        sut.updates.test {
            val toDelete = saved.get(0)
            val actual = sut.delete(toDelete, emit = true)
            assertTrue(actual.isSuccessful)
            assertEquals(DELETE to toDelete, awaitItem())
            expectNoEvents()
            val check = sut.load(toDelete.id!!, true)
            assertFalse(check.isSuccessful)
        }
    }

    @Test
    fun deleteAndUndo() = runTest {
        val (_, i) = dataCreation.createPlaylistAndItem()

        val item = sut.load(i.id).data!!
        sut.delete(item, false)
        val saved = sut.save(item, true, false).data!!
        assertEquals(item, saved)
    }


//    @Test
//    fun loadStatsList() {
//    }
//
//    @Test
//    fun count() {
//    }

//    @Test
//    fun deleteAll() {
//    }
//
//    @Test
//    fun update() {
//    }
}