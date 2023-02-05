package uk.co.sentinelweb.cuer.db.repository

import app.cash.turbine.test
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
import uk.co.sentinelweb.cuer.app.db.repository.ConflictException
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistItemDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.RepoResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.db.util.DataCreation
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.db.util.kotlinFixtureDefaultConfig
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.toGUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SqldelightPlaylistItemDatabaseRepositoryTest : KoinTest {
    private val fixture = kotlinFixtureDefaultConfig

    // todo test NewMediaFilter RecentMediaFilter SearchFilter loadStatsList()

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
    val guidCreator: GuidCreator by inject()

    val sut: PlaylistItemDatabaseRepository by inject()
    val mediaRepo: MediaDatabaseRepository by inject()

    val log: LogWrapper by inject()

    lateinit var dataCreation: DataCreation
    private val source = LOCAL

    @Before
    fun before() {
        Database.Schema.create(get())
        dataCreation = DataCreation(database, fixture, get())
        log.tag(this)
    }

    @Test
    fun loadFlat() = runTest {
        val (_, itemEntity) = dataCreation.createPlaylistAndItem()
        val expectedDomain = itemMapper.map(itemEntity, mediaRepo.load(itemEntity.media_id.toGUID(), flat = false).data!!)
        val actual = sut.load(itemEntity.id.toGUID(), flat = false)
        assertTrue(actual.isSuccessful)
        assertEquals(expectedDomain, actual.data)
    }

    @Test
    fun loadDoesntExist() = runTest {
        val actual = sut.load(guidCreator.create(), flat = false)
        assertFalse(actual.isSuccessful)
    }

    @Test
    fun createFlat() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val toCreate =
            fixture<PlaylistItemDomain>()
                .run { copy(playlistId = playlistEntity.id.toGuidIdentifier(source), media = media.copy(id = null)) }
        sut.updates.test {
            val actual = sut.save(toCreate, flat = true, emit = true)
            assertTrue(actual.isSuccessful)
            assertNotNull(actual.data)
            val expectedLoaded = sut.load(actual.data!!.id!!.id, flat = false)
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
                .run { copy(playlistId = playlistEntity.id.toGuidIdentifier(source), media = media.copy(id = null)) }
        val created = sut.save(toCreate, flat = true, emit = true)
        assertTrue(created.isSuccessful)
        assertNotNull(created.data)

        val toUpdate = fixture<PlaylistItemDomain>()
            .run {
                copy(
                    id = created.data!!.id!!,
                    playlistId = playlistEntity.id.toGuidIdentifier(source),
                    media = created.data!!.media
                )
            }

        sut.updates.test {
            val updated = sut.save(toUpdate, flat = true, emit = true)
            assertTrue(updated.isSuccessful)
            assertNotNull(updated.data)
            val expectedLoaded = sut.load(created.data!!.id!!.id, flat = false)
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
                .run { copy(playlistId = playlistEntity.id.toGuidIdentifier(source), media = media.copy(id = null)) }
        val created = sut.save(toCreate, flat = true, emit = false)
        assertTrue(created.isSuccessful)
        assertNotNull(created.data)

        val toUpdate = fixture<PlaylistItemDomain>()
            .run {
                copy(
                    id = created.data!!.id!!,
                    playlistId = playlistEntity.id.toGuidIdentifier(source),
                    media = media.copy(
                        id = created.data!!.media.id,
                        platformId = created.data!!.media.platformId,
                        platform = created.data!!.media.platform
                    )// should update the media in the db
                )
            }

        sut.updates.test {
            val updated = sut.save(toUpdate, flat = false, emit = true)
            assertTrue(updated.isSuccessful)
            assertNotNull(updated.data)
            val expectedLoaded = sut.load(created.data!!.id!!.id, flat = false)
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
            dataCreation.fixturePlaylistItemList()
                .map { it.copy(playlistId = playlistEntity.id.toGuidIdentifier(source), media = it.media.copy(id = null)) }
        sut.updates.test {
            val created = sut.save(toCreate, flat = true, emit = true)
            assertTrue(created.isSuccessful)
            assertNotNull(created.data)
            val actual = created.data!!
            val expected = actual.map { sut.load(it.id!!.id, flat = false).data!! }
            assertEquals(expected, actual)

            expected.forEach {
                assertEquals(FLAT to it, awaitItem())
            }
            expectNoEvents()
        }
    }

    @Test
    fun loadListByIds() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val list =
            dataCreation.fixturePlaylistItemList()
                .map { it.copy(playlistId = playlistEntity.id.toGuidIdentifier(source), media = it.media.copy(id = null)) }
        val saved = sut.save(list, true, false).data!!
        val ids = listOf(list[0].id!!.id, list[2].id!!.id)
        val actual = sut.loadList(IdListFilter(ids), flat = false).data!!.sortedBy { it.id!!.id.value }
        val expected = saved.filter { ids.contains(it.id!!.id) }.sortedBy { it.id!!.id.value }
        assertEquals(expected, actual)
    }

    @Test
    fun loadListByMediaIds() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val list =
            dataCreation.fixturePlaylistItemList()
                .map { it.copy(playlistId = playlistEntity.id.toGuidIdentifier(source), media = it.media.copy(id = null)) }
        val saved = sut.save(list, true, false).data!!
        val ids = listOf(saved[0].media.id!!.id, saved[2].media.id!!.id)
        val actual = sut.loadList(MediaIdListFilter(ids), flat = false).data!!.sortedBy { it.id!!.id.value }
        val expected = saved.filter { ids.contains(it.media.id!!.id) }.sortedBy { it.id!!.id.value }
        assertEquals(expected, actual)
    }

    @Test
    fun loadListByPlatformIds() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val list = dataCreation.fixturePlaylistItemList()
            .mapIndexed { i, item ->
                item.copy(
                    playlistId = playlistEntity.id.toGuidIdentifier(source),
                    media = item.media.copy(id = null, platformId = "platformId_$i")
                )
            }
        val saved = sut.save(list, true, false).data!!
        val platformIds = listOf(list[0].media.platformId, list[2].media.platformId)
        val actual = sut.loadList(PlatformIdListFilter(platformIds), flat = false).data!!
        assertEquals(platformIds.size, actual.size)
        assertEquals(saved.filter { platformIds.contains(it.media.platformId) }, actual)
    }

    @Test
    fun loadListByChannelId() = runTest {
        val (_, itemEntity1) = dataCreation.createPlaylistAndItem()
        val (_, itemEntity2) = dataCreation.createPlaylistAndItem()
        val (_, itemEntity3) = dataCreation.createPlaylistAndItem()
        val (_, _) = dataCreation.createPlaylistAndItem()

        val itemIds = listOf(itemEntity1.id.toGUID(), itemEntity2.id.toGUID(), itemEntity3.id.toGUID())
        val listItems = sut
            .loadList(IdListFilter(ids = itemIds), flat = false)
            .data!!
        val channelDomain = listItems[0].media.channelData
        val listItemsModified = listItems.map { it.copy(media = it.media.copy(channelData = channelDomain)) }
        val listItemsSaved = sut.save(listItemsModified, flat = false, emit = false).data!!
        assertEquals(listItemsModified, listItemsSaved)
        val actual =
            sut.loadList(ChannelPlatformIdFilter(platformId = channelDomain.platformId!!), flat = false).data!!
        assertEquals(listItemsModified, actual)
    }

    @Test
    fun delete() = runTest {
        val playlistEntity = dataCreation.createPlaylist()
        val list =
            dataCreation.fixturePlaylistItemList()
                .map { it.copy(playlistId = playlistEntity.id.toGuidIdentifier(source), media = it.media.copy(id = null)) }
        val saved = sut.save(list, true, false).data!!
        sut.updates.test {
            val toDelete = saved.get(0)
            val actual = sut.delete(toDelete, emit = true)
            assertTrue(actual.isSuccessful)
            assertEquals(DELETE to toDelete, awaitItem())
            expectNoEvents()
            val check = sut.load(toDelete.id!!.id, true)
            assertFalse(check.isSuccessful)
        }
    }

    @Test
    fun deleteAndUndo() = runTest {
        val (_, i) = dataCreation.createPlaylistAndItem()

        val item = sut.load(i.id.toGUID(), flat = false).data!!
        sut.delete(item, false)
        val saved = sut.save(item, true, false).data!!
        assertEquals(item, saved)
    }

    @Test
    fun count() = runTest {
        val initial = (1..5).map { dataCreation.createPlaylistAndItem() }
        val actual = sut.count(AllFilter).data!!
        assertEquals(initial.size, actual)
    }

    @Test
    fun onConflict_insert() = runTest {
        val (_, itemEntity1) = dataCreation.createPlaylistAndItem()
        val itemDomain1 = sut.load(itemEntity1.id.toGUID(), flat = false).data!!
        val itemConflict =
            fixture<PlaylistItemDomain>()
                .copy(
                    id = null,
                    playlistId = itemDomain1.playlistId,
                    media = itemDomain1.media,
                    order = itemDomain1.order
                )

        // returns the original item
        val savedConflict = sut.save(itemConflict, flat = false, emit = false)
        assertTrue(savedConflict.isSuccessful)
        assertEquals(itemDomain1, savedConflict.data!!)
    }

    @Test
    fun onConflict_list_insert() = runTest {
        val (_, itemEntity1) = dataCreation.createPlaylistAndItem()
        val itemDomain1 = sut.load(itemEntity1.id.toGUID(), flat = false).data!!
        val itemConflict =
            listOf(
                fixture<PlaylistItemDomain>()
                    .copy(
                        id = null,
                        playlistId = itemDomain1.playlistId,
                        media = itemDomain1.media,
                        order = itemDomain1.order
                    )
            )
        // returns the original item
        val savedConflict = sut.save(itemConflict, flat = false, emit = false)
        assertTrue(savedConflict.isSuccessful)
        assertEquals(itemDomain1, savedConflict.data!![0])
    }

    // if we try to save the same media on same playlist with different ordering then it will be an exception which is ok
    @Test
    fun onConflict_insert_differentOrder() = runTest {
        val (_, itemEntity1) = dataCreation.createPlaylistAndItem()
        val itemDomain1 = sut.load(itemEntity1.id.toGUID(), flat = false).data!!
        val itemConflict =
            fixture<PlaylistItemDomain>()
                .copy(
                    id = null,
                    playlistId = itemDomain1.playlistId,
                    media = itemDomain1.media,
                    archived = itemDomain1.archived.not(),
                )

        val actual = sut.save(itemConflict, flat = false, emit = false)
        assertFalse(actual.isSuccessful)
        assertEquals((actual as RepoResult.Error<*>).t::class, ConflictException::class)
    }

    @Test
    fun onConflict_update() = runTest {
        val (_, itemEntity1) = dataCreation.createPlaylistAndItem()
        val (_, itemEntity2) = dataCreation.createPlaylistAndItem()
        val itemDomain1 = sut.load(itemEntity1.id.toGUID(), flat = false).data!!
        val itemDomain2 = sut.load(itemEntity2.id.toGUID(), flat = false).data!!
        val itemConflict =
            fixture<PlaylistItemDomain>()
                .copy(
                    id = itemDomain2.id, // call update on 2nd item
                    playlistId = itemDomain1.playlistId,
                    media = itemDomain1.media,
                    order = itemDomain1.order
                )
        val actual = sut.save(itemConflict, flat = false, emit = false)
        assertFalse(actual.isSuccessful)
        assertEquals((actual as RepoResult.Error<*>).t::class, ConflictException::class)
    }

    @Test
    fun deleteAll() = runTest {
        (1..5).map { dataCreation.createPlaylistAndItem() }

        val deleted = sut.deleteAll()
        assertTrue(deleted.isSuccessful)

        val check = sut.count(AllFilter)
        assertTrue(check.isSuccessful)
        assertEquals(0, check.data!!)
    }
}