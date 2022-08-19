package uk.co.sentinelweb.cuer.app.db.repository

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
import uk.co.sentinelweb.cuer.domain.ImageDomain
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqldelightImageDatabaseRepositoryTest : KoinTest {

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
    val sut: ImageDatabaseRepository by inject()

    @Before
    fun before() {
        Database.Schema.create(get())
    }

    @Test
    fun create() = runTest {
        val initial = fixture<ImageDomain>().copy(id = null)
        with(sut.save(initial)) {
            assertTrue(isSuccessful)
            assertEquals(1L, data?.id)
            assertEquals(initial.url, data?.url)
            assertEquals(initial.width, data?.width)
            assertEquals(initial.height, data?.height)
        }
    }

    @Test
    fun updateImage() = runTest {
        val initial = fixture<ImageDomain>().copy(id = 10001)
        val repoResult = sut.save(initial)
        assertTrue(repoResult.isSuccessful)

        val update = fixture<ImageDomain>().copy(id = initial.id)
        val repoResultUpdate = sut.save(update)
        assertTrue(repoResultUpdate.isSuccessful)

        with(sut.load(initial.id!!)) {
            assertEquals(initial.id, data?.id)
            assertEquals(update.url, data?.url)
            assertEquals(update.width, data?.width)
            assertEquals(update.height, data?.height)
        }

    }

    @Test
    fun saveList() = runTest {
        val initial = fixture<List<ImageDomain>>()
            .map { it.copy(id = null) }
        val repoResult = sut.save(initial)
        assertTrue(repoResult.isSuccessful)
        repoResult.data!!.forEachIndexed { i, imageDomain ->
            assertEquals((i + 1).toLong(), imageDomain.id)
        }
    }

    @Test
    fun load() = runTest {
        val initial = fixture<ImageDomain>().copy(id = null)
        val repoResult = sut.save(initial)
        assertTrue(repoResult.isSuccessful)
        val saved = repoResult.data!!

        with(sut.load(saved.id!!)) {
            assertEquals(saved.id, data?.id)
            assertEquals(saved.url, data?.url)
            assertEquals(saved.width, data?.width)
            assertEquals(saved.height, data?.height)
        }
    }

    @Test
    fun deleteAll() = runTest {
        val initial = fixture<List<ImageDomain>>()
            .map { it.copy(id = null) }
        val repoResult = sut.save(initial)
        assertTrue(repoResult.isSuccessful)

        val deleteResult = sut.deleteAll()
        assertTrue(deleteResult.isSuccessful)
        val count = database.imageEntityQueries.count().executeAsOne()
        assertEquals(0, count)
    }

    @Test
    fun `loadEntity$Cuer_database_commonMain`() = runTest {
        val initial = fixture<ImageDomain>().copy(id = null)
        val repoResult = sut.save(initial)
        assertTrue(repoResult.isSuccessful)
        val saved = repoResult.data!!

        val sutImpl = sut as SqldelightImageDatabaseRepository
        with(sutImpl.loadEntity(saved.id!!)!!) {
            assertEquals(saved.id, id)
            assertEquals(saved.url, url)
            assertEquals(saved.width?.toLong(), width)
            assertEquals(saved.height?.toLong(), height)
        }
    }

    @Test
    fun `checkToSaveImage$Cuer_database_commonMain_create`() = runTest {
        val initial = fixture<ImageDomain>().copy(id = null)
        val sutImpl = sut as SqldelightImageDatabaseRepository
        with (sutImpl.checkToSaveImage(initial)) {
            assertEquals(1L, id)
            assertEquals(initial.url, url)
            assertEquals(initial.width, width)
            assertEquals(initial.height, height)
        }
    }

    @Test
    fun `checkToSaveImage$Cuer_database_commonMain_update_no_id_url`() = runTest {
        val initial = fixture<ImageDomain>().copy(id = null)
        val repoResult = sut.save(initial)
        assertTrue(repoResult.isSuccessful)
        val saved = repoResult.data!!

        val update = fixture<ImageDomain>().copy(id = null, url = initial.url)
        val sutImpl = sut as SqldelightImageDatabaseRepository
        with (sutImpl.checkToSaveImage(update)) {
            assertEquals(saved.id, id)
            assertEquals(initial.url, url)
            assertEquals(update.width, width)
            assertEquals(update.height, height)
        }
    }

    @Test
    fun `checkToSaveImage$Cuer_database_commonMain_update_id`() = runTest {
        val initial = fixture<ImageDomain>().copy(id = null)
        val repoResult = sut.save(initial)
        assertTrue(repoResult.isSuccessful)
        val saved = repoResult.data!!

        val update = fixture<ImageDomain>().copy(id = saved.id, url = initial.url)
        val sutImpl = sut as SqldelightImageDatabaseRepository
        with (sutImpl.checkToSaveImage(update)) {
            assertEquals(saved.id, id)
            assertEquals(update.url, url)
            assertEquals(update.width, width)
            assertEquals(update.height, height)
        }
    }

//    @Test
//    fun loadList() {
//    }
//
//    @Test
//    fun loadStatsList() {
//    }
//
//    @Test
//    fun count() {
//    }
//
//    @Test
//    fun delete() {
//    }
//    @Test
//    fun update() {
//    }
}