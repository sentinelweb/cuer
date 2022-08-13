package uk.co.sentinelweb.cuer.app.db.entity

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
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
import uk.co.sentinelweb.cuer.app.db.Image
import uk.co.sentinelweb.cuer.app.db.di.DatabaseModule
import uk.co.sentinelweb.cuer.app.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.app.db.util.MainCoroutineRule
import kotlin.test.assertEquals

class ImageEntityTest : KoinTest {
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

    @Before
    fun before() {
        Database.Schema.create(get())
    }

    @Test
    fun createLoadEntity() {
        val initial = fixture<Image>().copy(id = 0)
        database.imageEntityQueries.create(initial)
        val insertId = database.imageEntityQueries.getInsertId().executeAsOne()
        val actual = database.imageEntityQueries.load(1).executeAsOne()
        assertEquals(initial.copy(id = insertId), actual)
    }

    @Test
    fun update() {
        val initial = fixture<Image>().copy(id = 0)
        database.imageEntityQueries.create(initial)
        val insertId = database.imageEntityQueries.getInsertId().executeAsOne()
        val update = fixture<Image>().copy(id = insertId)
        database.imageEntityQueries.update(update)
        val actual = database.imageEntityQueries.load(insertId).executeAsOne()
        assertEquals(update, actual)
    }

    @Test
    fun deleteAll() {
        val initial = fixture<List<Image>>().map { it.copy(id = 0) }
        database.imageEntityQueries.transaction {
            initial.forEach {
                database.imageEntityQueries.create(it)
            }
        }

        database.channelEntityQueries.deleteAll()
        val actual = database.channelEntityQueries.count().executeAsOne()
        assertEquals(0, actual.toInt())
    }

    @Test
    fun createDeleteEntity() {
        val initial = fixture<Image>().copy(id = 0)
        database.imageEntityQueries.create(initial)
        val insertId = database.imageEntityQueries
            .getInsertId()
            .executeAsOne()
        database.imageEntityQueries.delete(insertId)
        val actual = database.imageEntityQueries
            .count()
            .executeAsOne()
        assertEquals(0, actual.toInt())
    }

    @Test
    fun loadByUrl() {
        val initial = fixture<Image>().copy(id = 0)
        database.imageEntityQueries.create(initial)
        val insertId = database.imageEntityQueries
            .getInsertId()
            .executeAsOne()

        val actual = database.imageEntityQueries
            .loadByUrl(initial.url)
            .executeAsOne()
        assertEquals(insertId, actual.id)
    }
}