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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.AllFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.IdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.DELETE
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain
import kotlin.test.*

class SqldelightMediaDatabaseRepositoryTest : KoinTest {
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
    val channelDatabaseRepository: ChannelDatabaseRepository by inject()

    val sut: MediaDatabaseRepository by inject()

    @Before
    fun before() {
        Database.Schema.create(get())
    }

    @Test
    fun saveCreate() = runTest {
        val initial = fixture<MediaDomain>()
        sut.updates.test {
            val saved = sut.save(initial, emit = true, flat = false).data!!

            val expected = initial.copy(
                id = saved.id,
                channelData = initial.channelData.copy(
                    id = saved.channelData.id,
                    thumbNail = initial.channelData.thumbNail?.copy(id = saved.channelData.thumbNail?.id),
                    image = initial.channelData.image?.copy(id = saved.channelData.image?.id)
                ),
                thumbNail = initial.thumbNail?.copy(id = saved.thumbNail?.id),
                image = initial.image?.copy(id = saved.image?.id)
            )

            assertEquals(expected, saved)
            assertEquals(FULL to saved, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun saveCreateChannelExists() = runTest {
        val initial = fixture<MediaDomain>()
        val savedChannel = channelDatabaseRepository.save(initial.channelData).data!!
        val initialWithChannel = initial.copy(channelData = savedChannel)
        sut.updates.test {
            val saved = sut.save(initialWithChannel, emit = true, flat = false).data!!

            val expected = initialWithChannel.copy(
                id = saved.id,
                thumbNail = initial.thumbNail?.copy(id = saved.thumbNail?.id),
                image = initial.image?.copy(id = saved.image?.id)
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
        val initial = fixture<MediaDomain>()
        val initialSaved = sut.save(initial, emit = false, flat = false).data!!
        sut.updates.test {
            val changed = fixture<MediaDomain>().copy(
                id = initialSaved.id,
                channelData = initialSaved.channelData,
                thumbNail = initialSaved.thumbNail,
                image = initialSaved.image
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
        val initial = fixture<List<MediaDomain>>()
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
                    image = media.image?.copy(id = saved[i].image?.id)
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
        val initial = fixture<List<MediaDomain>>()
        val initialSaved = sut.save(initial, emit = false, flat = false).data!!
        sut.updates.test {
            val changed = initialSaved.map {
                fixture<MediaDomain>().copy(
                    id = it.id,
                    channelData = it.channelData,
                    thumbNail = it.thumbNail,
                    image = it.image
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
        val expectedIds = expected.map { it.id!! }

        val actual = sut.loadList(filter = IdListFilter(expectedIds))

        assertEquals(expected, actual.data)
    }

    @Test
    fun loadListByPlatformIds() = runTest {
        val initialSaved = addMediaToDb()

        val expected = initialSaved
            .filterIndexed { index, _ -> index.mod(2) == 0 }
        val expectedIds = expected.map { it.platformId }

        val actual = sut.loadList(filter = OrchestratorContract.PlatformIdListFilter(expectedIds))

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

        val actual = sut.count(AllFilter()).data!!

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
        val tryToLoadDeleted = sut.load(toDelete.id!!)
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
        val saved = sut.save(initial).data
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
        val saved = sut.save(initial).data!!
        sut.updates.test {
            val update = fixture<MediaPositionUpdateDomain>().copy(id = saved.id!!)
            val actual = sut.update(update, emit = true)
            val expected = sut.load(saved.id!!)
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

        val actual = sut.save(duplicatePlatformId)
        assertFalse(actual.isSuccessful)
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

        val actual = sut.save(duplicatePlatformIdList)
        assertFalse(actual.isSuccessful)
    }

    @Test
    fun onConflict_update() = runTest {
        val initialSaved = addMediaToDb()
        val duplicatePlatformId = fixture<MediaDomain>().run {
            copy(
                title = "duplicate title",
                id = 2,
                platform = initialSaved[0].platform,
                platformId = initialSaved[0].platformId,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        }

        val actual = sut.save(duplicatePlatformId)
//        println("--- to save")
//        println(duplicatePlatformId.summarise())
//        println("--- actual")
//        println(actual.isSuccessful)
//        println(actual.data?.summarise())
//        println("--- db")
//        println(sut.loadList(AllFilter()).data?.map { it.summarise() }?.joinToString("\n"))

        val load = sut.load(duplicatePlatformId.id!!).data!!
        // The item won't save as it do nothing - but the db record won't be changed
        assertTrue(actual.isSuccessful)
        assertNotEquals(load, duplicatePlatformId)
    }

    @Test
    fun onConflict_list_update() = runTest {
        val initialSaved = addMediaToDb()
        val duplicatePlatformId = listOf(fixture<MediaDomain>().run {
            copy(
                title = "duplicate title",
                id = 2,
                platform = initialSaved[0].platform,
                platformId = initialSaved[0].platformId,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        })

        val actual = sut.save(duplicatePlatformId)
        val load = sut.load(duplicatePlatformId[0].id!!).data!!
        // The item won't save as it do nothing - but the db record won't be changed
        assertTrue(actual.isSuccessful)
        assertNotEquals(load, duplicatePlatformId[0])
    }
}