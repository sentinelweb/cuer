package uk.co.sentinelweb.cuer.app.db.repository

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
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
import uk.co.sentinelweb.cuer.app.db.di.DatabaseModule
import uk.co.sentinelweb.cuer.app.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.app.db.util.MainCoroutineRule
import uk.co.sentinelweb.cuer.domain.ChannelDomain

class SqldelightChannelDatabaseRepositoryTest : KoinTest {
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
                .plus(DatabaseModule.modules)
                .plus(dbIntegrationRule.modules)
                .plus(mainCoroutineRule.modules)
        )
    }

    val database: Database by inject()
    val sut: ChannelDatabaseRepository by inject()

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
        val savedImage = initial.image
            ?.let { database.imageEntityQueries.loadByUrl(it.url) }
            ?.executeAsOneOrNull()
        val savedThumb = initial.thumbNail
            ?.let { database.imageEntityQueries.loadByUrl(it.url) }
            ?.executeAsOneOrNull()
        with(sut.save(initial)) {
            assertTrue(isSuccessful)
            assertEquals(1L, data?.id)
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
                    assertEquals(savedImage!!.url, data!!.image!!.url)
                    assertEquals(savedImage.width, data!!.image!!.width)
                    assertEquals(savedImage.url, data!!.image!!.url)
                }
                ?: apply { assertNull(data?.image) }
            initial.thumbNail
                ?.apply {
                    assertNotNull(data!!.image!!.id)
                    assertEquals(savedThumb!!.url, data!!.thumbNail!!.url)
                    assertEquals(savedThumb.width, data!!.thumbNail!!.width)
                    assertEquals(savedThumb.url, data!!.thumbNail!!.url)
                }
                ?: apply { assertNull(data?.thumbNail) }
        }
    }

    @Test
    fun testSave() {
    }

    @Test
    fun load() {
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
    fun update() {
    }

    @Test
    fun deleteAll() {
    }

    @Test
    fun `loadChannel$Cuer_database_commonMain`() {
    }

    @Test
    fun `checkToSaveChannel$Cuer_database_commonMain`() {
    }
}