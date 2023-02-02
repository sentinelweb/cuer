package uk.co.sentinelweb.cuer.db.repository

import app.cash.turbine.test
import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.*
import uk.co.sentinelweb.cuer.db.mapper.PlaylistItemMapper
import uk.co.sentinelweb.cuer.db.mapper.PlaylistMapper
import uk.co.sentinelweb.cuer.db.util.DataCreation
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.db.util.resetIds
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.toGUID
import uk.co.sentinelweb.cuer.domain.update.PlaylistIndexUpdateDomain
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SqldelightPlaylistDatabaseRepositoryTest : KoinTest {
    private val fixture = kotlinFixture {
        nullabilityStrategy(NeverNullStrategy)
        factory { OrchestratorContract.Identifier(GuidCreator().create(), fixture()) }
    }

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
        dataCreation = DataCreation(database, fixture, get())
    }

    @Test
    fun createFlat() = runTest {
        val toCreate = fixture<PlaylistDomain>().resetIds()
        sut.updates.test {
            val actual = sut.save(toCreate, flat = true, emit = true)
            assertTrue(actual.isSuccessful)
            val expected = sut.load(actual.data!!.id!!.id, flat = true)
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
            val expected = sut.load(actual.data!!.id!!.id, flat = true)
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
            val expected = sut.load(actual.data!!.id!!.id, flat = false)
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
            val expected = sut.load(actual.data!!.id!!.id, flat = false)
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
                val expected = sut.load(it.id!!.id, flat = false)
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
            playlistEntity.channel_id?.let { channelRepo.load(it.toGUID(), flat = false).data!! },
            playlistEntity.thumb_id?.let { imageRepo.loadEntity(it.toGUID()) },
            playlistEntity.image_id?.let { imageRepo.loadEntity(it.toGUID()) },
        )
        val actual = sut.load(playlistEntity.id.toGUID(), flat = true)
        assertTrue(actual.isSuccessful)
        assertEquals(expectedDomain, actual.data)
    }

    @Test
    fun loadFull() = runTest {
        val (playlistEntity, itemEntity) = dataCreation.createPlaylistAndItem()
        val expectedDomain = playlistMapper.map(
            playlistEntity,
            listOf(itemMapper.map(itemEntity, mediaRepo.load(itemEntity.media_id.toGUID(), flat = false).data!!)),
            playlistEntity.channel_id?.let { channelRepo.load(it.toGUID(), flat = false).data!! },
            playlistEntity.thumb_id?.let { imageRepo.loadEntity(it.toGUID()) },
            playlistEntity.image_id?.let { imageRepo.loadEntity(it.toGUID()) },
        )
        val actual = sut.load(playlistEntity.id.toGUID(), flat = false)
        assertTrue(actual.isSuccessful)
        assertEquals(expectedDomain, actual.data)
    }

    @Test
    fun loadList_IdListFilter() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>().map { it.resetIds() }
        val saved = sut.save(toCreate, flat = false, emit = false)
        assertTrue(saved.isSuccessful)
        val ids = saved.data?.map { it.id!!.id }?.take(2)!!
        val loaded = sut.loadList(IdListFilter(ids), flat = false)
        assertTrue(loaded.isSuccessful)
        val expected = saved.data!!.filter { ids.contains(it.id!!.id) }.sortedBy { it.id!!.id.value }
        val actual = loaded.data!!.sortedBy { it.id!!.id.value }
        assertEquals(expected, actual)
    }

    @Test
    fun loadList_IdListFilter_flat() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>().map { it.resetIds() }
        val saved = sut.save(toCreate, flat = false, emit = false)
        assertTrue(saved.isSuccessful)
        val ids = saved.data?.map { it.id!!.id }?.take(2)!!
        val loaded = sut.loadList(IdListFilter(ids), flat = true)
        assertTrue(loaded.isSuccessful)
        val expected = saved.data!!
            .filter { ids.contains(it.id!!.id) }
            .map { it.copy(items = listOf()) }
            .sortedBy { it.id!!.id.value }
        assertEquals(expected, loaded.data!!.sortedBy { it.id!!.id.value })
        loaded.data!!.forEach { assertEquals(0, it.items.size) }
    }

    @Test
    fun loadList_DefaultFilter() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>()
            .map { it.resetIds() }
            .mapIndexed { i, item -> item.copy(default = i == 0) }

        val saved = sut.save(toCreate, flat = false, emit = false)
        assertTrue(saved.isSuccessful)
        val loaded = sut.loadList(DefaultFilter, flat = false)
        assertTrue(loaded.isSuccessful)
        assertEquals(1, loaded.data!!.size)
        assertEquals(saved.data!![0], loaded.data!![0])
    }

    @Test
    fun loadList_AllFilter() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>()
            .map { it.resetIds() }

        val saved = sut.save(toCreate, flat = false, emit = false)
        assertTrue(saved.isSuccessful)
        val loaded = sut.loadList(AllFilter, flat = false)
        assertTrue(loaded.isSuccessful)
        assertEquals(saved.data!!, loaded.data!!)
    }

    @Test
    fun loadList_PlatformIdListFilter() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>()
            .map { it.resetIds() }
            .map { it.copy(platform = PlatformDomain.PODCAST) }

        val saved = sut.save(toCreate, flat = false, emit = false)
        assertTrue(saved.isSuccessful)
        val platformIds = saved.data!!.take(2).mapNotNull { it.platformId }
        val loaded = sut.loadList(PlatformIdListFilter(platformIds, platform = PlatformDomain.PODCAST), flat = false)
        assertTrue(loaded.isSuccessful)
        assertEquals(platformIds.size, loaded.data!!.size)
        assertEquals(saved.data!!.filter { platformIds.contains(it.platformId) }, loaded.data!!)
    }

    @Test
    @Ignore(value = "fixme doesnt load channel") // fixme doesnt load channel
    fun loadList_ChannelPlatformIdFilter() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>()
            .map { it.resetIds() }

        val saved = sut.save(toCreate, flat = false, emit = false)
        assertTrue(saved.isSuccessful)
        val chanPlatformId = saved.data!!.firstNotNullOf { it.channelData?.platformId }
        val loaded = sut.loadList(ChannelPlatformIdFilter(chanPlatformId), flat = false)
        assertTrue(loaded.isSuccessful)
        assertEquals(1, loaded.data!!.size)
        assertEquals(saved.data!!.filter { it.channelData?.platformId == chanPlatformId }, loaded.data!!)
    }

    @Test
    fun loadList_TitleFilter() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>()
            .map { it.resetIds() }

        val saved = sut.save(toCreate, flat = false, emit = false)
        assertTrue(saved.isSuccessful)
        val title = saved.data!![0].title
        val loaded = sut.loadList(TitleFilter(title), flat = false)
        assertTrue(loaded.isSuccessful)
        assertEquals(1, loaded.data!!.size)
        assertEquals(saved.data!!.filterIndexed { i, pl -> i == 0 }, loaded.data!!)
    }

    @Test
    fun count() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>()
            .map { it.resetIds() }

        val saved = sut.save(toCreate, flat = false, emit = false)
        assertTrue(saved.isSuccessful)
        val loaded = sut.count(AllFilter)
        assertTrue(loaded.isSuccessful)
        assertEquals(saved.data!!.size, loaded.data!!)
    }

    @Test
    fun delete() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>()
            .map { it.resetIds() }

        val saved = sut.save(toCreate, flat = false, emit = false)
        assertTrue(saved.isSuccessful)
        val toDelete = saved.data!![0]
        sut.updates.test {
            val deleted = sut.delete(toDelete, emit = true)
            assertTrue(deleted.isSuccessful)
            assertEquals(DELETE to toDelete, awaitItem())
            expectNoEvents()
        }
        val check = sut.load(toDelete.id!!.id, true)
        assertFalse(check.isSuccessful)
    }

    @Test
    fun deleteAll() = runTest {
        val toCreate = fixture<List<PlaylistDomain>>()
            .map { it.resetIds() }

        val saved = sut.save(toCreate, flat = false, emit = false)
        assertTrue(saved.isSuccessful)
        val deleted = sut.deleteAll()
        assertTrue(deleted.isSuccessful)

        val check = sut.count(AllFilter)
        assertTrue(check.isSuccessful)
        assertEquals(0, check.data!!)
    }

    @Test
    fun update() = runTest {
        val toCreate = fixture<PlaylistDomain>()
            .resetIds()
        val actual = sut.save(toCreate, flat = true, emit = true)
        assertTrue(actual.isSuccessful)
        sut.updates.test {
            val updated =
                sut.update(
                    PlaylistIndexUpdateDomain(id = actual.data!!.id!!, currentIndex = 1000),
                    flat = false,
                    emit = true
                )
            assertTrue(updated.isSuccessful)
            val expected = sut.load(actual.data!!.id!!.id, flat = true)
            assertTrue(expected.isSuccessful)
            assertEquals(1000, expected.data!!.currentIndex)

            assertEquals(FLAT to actual.data!!.copy(currentIndex = 1000), awaitItem())
            expectNoEvents()
        }
    }

    @Test // todo test
    fun loadStatsList() {
    }
}