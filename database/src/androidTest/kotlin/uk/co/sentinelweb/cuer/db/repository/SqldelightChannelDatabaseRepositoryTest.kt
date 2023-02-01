package uk.co.sentinelweb.cuer.db.repository

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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter.AllFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SqldelightChannelDatabaseRepositoryTest : KoinTest {

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

    private val database: Database by inject()

    private val sut: ChannelDatabaseRepository by inject()
    @Before
    fun before() {
        Database.Schema.create(get())
    }

    @Test
    fun createChannel() = runTest {
        val initial = fixture<ChannelDomain>().run {
            copy(
                id = null,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        }
        with(sut.save(initial, flat = false, emit = false)) {
            assertTrue(isSuccessful)
            //assertEquals(1L, data?.id)
            assertEquals(initial.platform, data?.platform)
            assertEquals(initial.platformId, data?.platformId)
            assertEquals(initial.country, data?.country)
            assertEquals(initial.title, data?.title)
            assertEquals(initial.customUrl, data?.customUrl)
            assertEquals(initial.description, data?.description)
            assertEquals(initial.published, data?.published)
            initial.image
                ?.apply {
                    assertNotNull(data!!.image!!.id)
                    assertEquals(initial.image!!.url, data!!.image!!.url)
                    assertEquals(initial.image!!.width, data!!.image!!.width)
                    assertEquals(initial.image!!.url, data!!.image!!.url)
                }
                ?: apply { assertNull(data?.image) }
            initial.thumbNail
                ?.apply {
                    assertNotNull(data!!.thumbNail!!.id)
                    assertEquals(initial.thumbNail!!.url, data!!.thumbNail!!.url)
                    assertEquals(initial.thumbNail!!.width, data!!.thumbNail!!.width)
                    assertEquals(initial.thumbNail!!.url, data!!.thumbNail!!.url)
                }
                ?: apply { assertNull(data?.thumbNail) }
        }
    }

    @Test
    fun saveList() = runTest {
        val initial = fixture<List<ChannelDomain>>().map { fixt ->
            fixt.copy(
                id = null,
                thumbNail = fixt.thumbNail?.copy(id = null),
                image = fixt.image?.copy(id = null),
            )
        }
        with(sut.save(initial, flat = false, emit = false)) {
            assertTrue(isSuccessful)
            assertNotNull(data)
            data!!.forEachIndexed { i, savedChannel ->
                //assertEquals((i + 1).toLong(), savedChannel.id)
                assertEquals(initial[i].platform, savedChannel.platform)
                assertEquals(initial[i].platformId, savedChannel.platformId)
                assertEquals(initial[i].country, savedChannel.country)
                assertEquals(initial[i].title, savedChannel.title)
                assertEquals(initial[i].customUrl, savedChannel.customUrl)
                assertEquals(initial[i].description, savedChannel.description)
                assertEquals(initial[i].published, savedChannel.published)
                initial[i].image
                    ?.apply {
                        assertNotNull(savedChannel.image!!.id)
                        assertEquals(initial[i].image!!.url, savedChannel.image!!.url)
                        assertEquals(initial[i].image!!.width, savedChannel.image!!.width)
                        assertEquals(initial[i].image!!.url, savedChannel.image!!.url)
                    }
                    ?: apply { assertNull(savedChannel.image) }
                initial[i].thumbNail
                    ?.apply {
                        assertNotNull(savedChannel.thumbNail!!.id)
                        assertEquals(initial[i].thumbNail!!.url, savedChannel.thumbNail!!.url)
                        assertEquals(initial[i].thumbNail!!.width, savedChannel.thumbNail!!.width)
                        assertEquals(initial[i].thumbNail!!.url, savedChannel.thumbNail!!.url)
                    }
                    ?: apply { assertNull(savedChannel.thumbNail) }
            }
        }
    }

    @Test
    fun load() = runTest {
        val initial = fixture<ChannelDomain>().run {
            copy(
                id = null,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        }
        val saved = sut.save(initial, flat = false, emit = false).data
        assertNotNull(saved)
        val actual = sut.load(saved.id!!.id, flat = false)
        assertEquals(saved, actual.data)
    }

    @Test
    fun `checkToSaveChannel platformId exists`() = runTest {
        val initial = fixture<ChannelDomain>().run {
            copy(
                id = null,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        }
        val saved = sut.save(initial, flat = false, emit = false).data
        assertNotNull(saved)
        val updated = fixture<ChannelDomain>().run {
            copy(
                id = null,
                platform = initial.platform,
                platformId = initial.platformId,
                thumbNail = initial.thumbNail,
                image = initial.image,
            )
        }

        val sutImpl = sut as SqldelightChannelDatabaseRepository
        val actual = sutImpl.checkToSaveChannel(updated)
        val expected = sutImpl.load(saved.id!!.id, flat = false).data
        assertEquals(expected, actual)
    }

    @Test
    fun `checkToSaveChannel platformId exists new images`() = runTest {
        val initial = fixture<ChannelDomain>().run {
            copy(
                id = null,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        }
        val saved = sut.save(initial, flat = false, emit = false).data
        assertNotNull(saved)
        val updated = fixture<ChannelDomain>().run {
            copy(
                id = null,
                platform = initial.platform,
                platformId = initial.platformId
            )
        }

        val sutImpl = sut as SqldelightChannelDatabaseRepository
        val actual = sutImpl.checkToSaveChannel(updated)
        val expected = sutImpl.load(saved.id!!.id, flat = false).data
        assertEquals(expected, actual)
    }

    @Test
    fun `checkToSaveChannel does not exist`() = runTest {
        val initial = fixture<ChannelDomain>().run {
            copy(
                id = null,
                thumbNail = thumbNail?.copy(id = null),
                image = image?.copy(id = null),
            )
        }

        val sutImpl = sut as SqldelightChannelDatabaseRepository
        val actual = sutImpl.checkToSaveChannel(initial)

        assertNotNull(actual.id)
        assertEquals(LOCAL, actual.id!!.source)
        assertNotNull(actual.id!!.id.value)

        initial.thumbNail?.apply {
            assertNotNull(actual.thumbNail!!.id)
            assertEquals(LOCAL, actual.thumbNail!!.id!!.source)
            assertNotNull(actual.thumbNail!!.id!!.id.value)
        }

        initial.image?.apply {
            assertNotNull(actual.image!!.id)
            assertEquals(LOCAL, actual.image!!.id!!.source)
            assertNotNull(actual.image!!.id!!.id.value)
        }
    }

    @Test
    fun count() = runTest {
        val initial = fixture<List<ChannelDomain>>().map {
            it.copy(
                id = null,
                thumbNail = it.thumbNail?.copy(id = null),
                image = it.image?.copy(id = null),
            )
        }
        initial.forEach { sut.save(it, flat = false, emit = false) }
        val actual = sut.count(AllFilter).data!!

        assertEquals(initial.size, actual)
    }

//    @Test
//    fun loadList() {
//    }

//    @Test
//    fun loadStatsList() {
//    }

//    @Test
//    fun count() {
//    }

    //    @Test
//    fun delete() {
//    }
    @Test
    fun deleteAll() = runTest {
        val initial = fixture<ChannelDomain>().run {
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
        val actualCount = database.channelEntityQueries.count().executeAsOne()
        assertEquals(0, actualCount)
    }
}