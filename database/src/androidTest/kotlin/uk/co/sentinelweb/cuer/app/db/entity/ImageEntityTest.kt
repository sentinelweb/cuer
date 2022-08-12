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
        database.imageEntityQueries.createImage(initial)
        val insertId = database.imageEntityQueries.getInsertIdImage().executeAsOne()
        val actual = database.imageEntityQueries.loadImage(1).executeAsOne()
        assertEquals(initial.copy(id = insertId), actual)
    }

    @Test
    fun update() {
        val initial = fixture<Image>().copy(id = 0)
        database.imageEntityQueries.createImage(initial)
        val insertId = database.imageEntityQueries.getInsertIdImage().executeAsOne()
        val update = fixture<Image>().copy(id = insertId)
        database.imageEntityQueries.update(update)
        val actual = database.imageEntityQueries.loadImage(insertId).executeAsOne()
        assertEquals(update, actual)
    }
}