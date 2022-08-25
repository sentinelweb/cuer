package uk.co.sentinelweb.cuer.app.db.repository

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
import uk.co.sentinelweb.cuer.app.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.app.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.IdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import kotlin.test.assertEquals

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
    }

    @Test
    fun delete() = runTest {
    }

    @Test
    fun deleteAll() = runTest {
    }

    @Test
    fun updatePosition() = runTest {
    }
}