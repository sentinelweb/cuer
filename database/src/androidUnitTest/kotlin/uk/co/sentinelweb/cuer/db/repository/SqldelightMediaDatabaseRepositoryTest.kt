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
import uk.co.sentinelweb.cuer.app.db.repository.ChannelDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.ConflictException
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.DbResult
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.DELETE
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.db.util.kotlinFixtureDefaultConfig
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain
import kotlin.test.*

class SqldelightMediaDatabaseRepositoryTest : KoinTest {

    private val fixture = kotlinFixtureDefaultConfig

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

    private val database: Database by inject()
    private val channelDatabaseRepository: ChannelDatabaseRepository by inject()
    private val guidCreator: GuidCreator by inject()

    val sut: MediaDatabaseRepository by inject()

    @Before
    fun before() {
        Database.Schema.create(get())
    }

    @Test
    fun saveCreate() = runTest {
        val initial = fixture<MediaDomain>().copy(id = null)
        sut.updates.test {
            val saved = sut.save(initial, emit = true, flat = false).data!!

            val expected = initial.copy(
                id = saved.id,
                platform = saved.platform,
                platformId = saved.platformId,
                channelData = initial.channelData.copy(
                    id = saved.channelData.id,
                    thumbNail = initial.channelData.thumbNail?.copy(id = saved.channelData.thumbNail?.id),
                    image = initial.channelData.image?.copy(id = saved.channelData.image?.id)
                ),
                thumbNail = initial.thumbNail?.copy(id = saved.thumbNail?.id),
                image = initial.image?.copy(id = saved.image?.id),
            )

            assertEquals(expected, saved)
            assertEquals(FULL to saved, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun saveCreateWithOtherIdSource() = runTest {
        val initial = fixture<MediaDomain>().copy(id = guidCreator.create().toIdentifier(MEMORY))
        sut.updates.test {
            val saved = sut.save(initial, emit = true, flat = false).data!!

            val expected = initial.copy(
                id = saved.id,
                platform = saved.platform,
                platformId = saved.platformId,
                channelData = initial.channelData.copy(
                    id = saved.channelData.id,
                    thumbNail = initial.channelData.thumbNail?.copy(id = saved.channelData.thumbNail?.id),
                    image = initial.channelData.image?.copy(id = saved.channelData.image?.id)
                ),
                thumbNail = initial.thumbNail?.copy(id = saved.thumbNail?.id),
                image = initial.image?.copy(id = saved.image?.id),
            )

            assertEquals(expected, saved)
            assertEquals(FULL to saved, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun saveCreateChannelExists() = runTest {
        val initial = fixture<MediaDomain>().copy(id = null)
        val savedChannel = channelDatabaseRepository.save(initial.channelData, flat = false, emit = false).data!!
        val initialWithChannel = initial.copy(channelData = savedChannel)
        sut.updates.test {
            val saved = sut.save(initialWithChannel, emit = true, flat = false).data!!

            val expected = initialWithChannel.copy(
                id = saved.id,
                platform = saved.platform,
                platformId = saved.platformId,
                thumbNail = initial.thumbNail?.copy(id = saved.thumbNail?.id),
                image = initial.image?.copy(id = saved.image?.id),
            )

            assertEquals(expected, saved)

            assertEquals(FULL to saved, awaitItem())
            expectNoEvents()
        }
        val channelCount = database.channelEntityQueries.count().executeAsOne()
        assertEquals(1, channelCount)
    }

    @Test
    fun saveUpdate() = runTest {
        val initial = fixture<MediaDomain>().copy(id = null)
        val initialSaved = sut.save(initial, emit = false, flat = false).data!!
        sut.updates.test {
            val changed = fixture<MediaDomain>().copy(
                id = initialSaved.id,
                platform = initialSaved.platform,
                platformId = initialSaved.platformId,
                channelData = initialSaved.channelData,
                thumbNail = initialSaved.thumbNail,
                image = initialSaved.image,
                broadcastDate = null,
            )
            val updated = sut.save(changed, emit = true, flat = false).data!!

            assertEquals(changed, updated)
            assertEquals(FULL to updated, awaitItem())
            expectNoEvents()
        }
        val channelCount = database.channelEntityQueries.count().executeAsOne()
        assertEquals(1, channelCount)
        val mediaCount = database.mediaEntityQueries.count().executeAsOne()
        assertEquals(1, mediaCount)
    }

    @Test
    fun saveListCreate() = runTest {
        val initial = fixture<List<MediaDomain>>().map { it.copy(id = null) }
        sut.updates.test {
            val saved = sut.save(initial, emit = true, flat = false).data!!

            val expected = initial.mapIndexed { i, media ->
                media.copy(
                    id = saved[i].id,
                    channelData = media.channelData.copy(
                        id = saved[i].channelData.id,
                        thumbNail = media.channelData.thumbNail?.copy(id = saved[i].channelData.thumbNail?.id),
                        image = media.channelData.image?.copy(id = saved[i].channelData.image?.id)
                    ),
                    thumbNail = media.thumbNail?.copy(id = saved[i].thumbNail?.id),
                    image = media.image?.copy(id = saved[i].image?.id),
                )
            }

            assertEquals(expected, saved)
            expected.forEach {
                assertEquals(FULL to it, awaitItem())
            }
            expectNoEvents()
        }
        val channelCount = database.channelEntityQueries.count().executeAsOne()
        assertEquals(initial.size.toLong(), channelCount)
        val mediaCount = database.mediaEntityQueries.count().executeAsOne()
        assertEquals(initial.size.toLong(), mediaCount)
    }

    @Test
    fun saveListUpdate() = runTest {
        val initial = fixture<List<MediaDomain>>().map { it.copy(id = null) }
        val initialSaved = sut.save(initial, emit = false, flat = false).data!!
        sut.updates.test {
            val changed = initialSaved.map {
                fixture<MediaDomain>().copy(
                    id = it.id,
                    platform = it.platform,
                    platformId = it.platformId,
                    channelData = it.channelData,
                    thumbNail = it.thumbNail,
                    image = it.image,
                    broadcastDate = null,
                )
            }
            val updated = sut.save(changed, emit = true, flat = false).data!!

            assertEquals(changed, updated)
            changed.forEach {
                assertEquals(FULL to it, awaitItem())
            }
            expectNoEvents()
        }
        val channelCount = database.channelEntityQueries.count().executeAsOne()
        assertEquals(initial.size.toLong(), channelCount)
        val mediaCount = database.mediaEntityQueries.count().executeAsOne()
        assertEquals(initial.size.toLong(), mediaCount)
    }

    @Test
    fun load() = runTest {
        // tested in save
    }

    @Test
    fun loadListByIds() = runTest {
        val initialSaved = addMediaToDb()

        val expected = initialSaved
            .filterIndexed { index, _ -> index.mod(2) == 0 }
            .sortedBy { it.id!!.id.value }
        val expectedIds = expected.map { it.id!!.id }

        val actual = sut.loadList(filter = IdListFilter(expectedIds), flat = false)

        assertEquals(expected, actual.data?.sortedBy { it.id!!.id.value })
    }

    @Test
    fun loadListByPlatformIds() = runTest {
        val initialSaved = addMediaToDb()

        val expected = initialSaved
            .filterIndexed { index, _ -> index.mod(2) == 0 }
        val expectedIds = expected.map { it.platformId }

        val actual = sut.loadList(filter = PlatformIdListFilter(expectedIds), flat = false)

        assertEquals(expected, actual.data)
    }

    private suspend fun addMediaToDb(): List<MediaDomain> {
        val initial = fixture<List<MediaDomain>>().map {
            it.copy(
                id = null,
                platform = PlatformDomain.YOUTUBE,
                channelData = it.channelData.copy(
                    id = null,
                    thumbNail = it.channelData.thumbNail?.copy(id = null),
                    image = it.channelData.image?.copy(id = null)
                ),
                thumbNail = it.thumbNail?.copy(id = null),
                image = it.image?.copy(id = null)
            )
        }
        val initialSaved = sut.save(initial, emit = false, flat = false).data!!
        return initialSaved
    }

    @Test
    fun count() = runTest {
        val initialSaved = addMediaToDb()

        val actual = sut.count(AllFilter).data!!

        assertEquals(initialSaved.size, actual)
    }

    @Test
    fun delete() = runTest {
        val initialSaved = addMediaToDb()

        val toDelete = initialSaved[1]
        sut.updates.test {
            val actual = sut.delete(toDelete, emit = true)
            assertEquals(true, actual.data!!)
            assertEquals(DELETE to toDelete, awaitItem())
        }
        val tryToLoadDeleted = sut.load(toDelete.id!!.id, flat = false)
        assertFalse { tryToLoadDeleted.isSuccessful }
    }

    @Test
    fun deleteAll() = runTest {
        val initial = fixture<MediaDomain>().run {
            copy(
                id = null,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        }
        val saved = sut.save(initial, flat = false, emit = false).data
        assertNotNull(saved)
        val actual = sut.deleteAll()
        assertEquals(true, actual.data)
        val actualCount = database.mediaEntityQueries.count().executeAsOne()
        assertEquals(0, actualCount)
    }

    @Test
    fun updatePosition() = runTest {
        val initial = fixture<MediaDomain>().run {
            copy(
                id = null,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        }
        val saved = sut.save(initial, flat = false, emit = false).data!!
        sut.updates.test {
            val update = fixture<MediaPositionUpdateDomain>().copy(id = saved.id!!)
            val actual = sut.update(update, flat = false, emit = true)
            val expected = sut.load(saved.id!!.id, flat = false)
            assertEquals(expected.data!!, actual.data!!)
            assertEquals(FULL to actual.data!!, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun onConflict_insert() = runTest {
        val initialSaved = addMediaToDb()
        val duplicatePlatformId = fixture<MediaDomain>().run {
            copy(
                id = null,
                platform = initialSaved[0].platform,
                platformId = initialSaved[0].platformId,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        }

        val actual = sut.save(duplicatePlatformId, flat = false, emit = false)
        assertTrue(actual.isSuccessful)
        assertEquals(initialSaved[0], actual.data)
    }

    @Test
    fun onConflict_insert_list() = runTest {
        val initialSaved = addMediaToDb()
        val duplicatePlatformIdList = listOf(fixture<MediaDomain>().run {
            copy(
                id = null,
                platform = initialSaved[0].platform,
                platformId = initialSaved[0].platformId,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        })

        val actual = sut.save(duplicatePlatformIdList, flat = false, emit = false)
//        assertFalse(actual.isSuccessful)
//        assertEquals((actual as RepoResult.Error<*>).t::class, ConflictException::class)
        assertTrue(actual.isSuccessful)
        assertEquals(listOf(initialSaved[0]), actual.data)
    }

    @Test
    fun onConflict_update() = runTest {
        val initialSaved = addMediaToDb()
        val duplicatePlatformId = fixture<MediaDomain>().run {
            copy(
                title = "duplicate title",
                id = initialSaved[2].id,
                platform = initialSaved[0].platform,
                platformId = initialSaved[0].platformId,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        }

        val actual = sut.save(duplicatePlatformId, flat = false, emit = false)
        // conflict error
        assertFalse(actual.isSuccessful)
//        println("--- to save")
//        println(duplicatePlatformId.summarise())
//        println("--- actual")
//        println(actual.isSuccessful)
//        println(actual.data?.summarise())
//        println("--- db")
//        println(sut.loadList(AllFilter()).data?.map { it.summarise() }?.joinToString("\n"))

        val load = sut.load(duplicatePlatformId.id!!.id, flat = false).data!!
        // fixme item isnt loadded from db - should be initial value
//        assertTrue(actual.isSuccessful)
//        assertEquals(initialSaved[2], load)
//        assertNotEquals(load, duplicatePlatformId)
    }

    @Test
    fun onConflict_list_update() = runTest {
        val initialSaved = addMediaToDb()
        val duplicatePlatformId = listOf(fixture<MediaDomain>().run {
            copy(
                title = "duplicate title",
                id = initialSaved[2].id,
                platform = initialSaved[0].platform,
                platformId = initialSaved[0].platformId,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        })

        val actual = sut.save(duplicatePlatformId, flat = false, emit = false)
        val load = sut.load(duplicatePlatformId[0].id!!.id, flat = false).data!!
        // The item won't save as it do nothing - but the db record won't be changed
        assertFalse(actual.isSuccessful)
        assertEquals((actual as DbResult.Error<*>).t::class, ConflictException::class)
        assertNotEquals(load, duplicatePlatformId[0])
    }
}
