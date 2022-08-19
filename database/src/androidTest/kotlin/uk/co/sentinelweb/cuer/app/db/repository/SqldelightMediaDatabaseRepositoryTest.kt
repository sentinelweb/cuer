package uk.co.sentinelweb.cuer.app.db.repository

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
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

class SqldelightMediaDatabaseRepositoryTest: KoinTest {
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
    val sut: MediaDatabaseRepository by inject()

    @Before
    fun before() {
        Database.Schema.create(get())
    }

    @Before
    fun setUp() {
    }

    @Test
    fun save() {
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
    fun count() {
    }

    @Test
    fun delete() {
    }

    @Test
    fun deleteAll() {
    }

    @Test
    fun update() {
    }
}