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
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.db.util.DataCreation
import uk.co.sentinelweb.cuer.db.util.DatabaseTestRule
import uk.co.sentinelweb.cuer.db.util.MainCoroutineRule

class SqldelightPlaylistDatabaseRepositoryTest: KoinTest {
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

    val sut: PlaylistDatabaseRepository by inject()

    lateinit var dataCreation: DataCreation

    @Before
    fun before() {
        Database.Schema.create(get())
        dataCreation = DataCreation(database, fixture)
    }

    @Test
    fun saveFlat() {
    }

    @Test
    fun saveFull() {
    }

    @Test
    fun saveList() {
    }

    @Test
    fun loadFlat() = runTest{
        val createPlaylist = dataCreation.createPlaylist()
        // todo need to do save first
        sut.load(createPlaylist.id)
    }

    @Test
    fun loadFull() {
        dataCreation.createPlaylistAndItem()
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
    fun deleteAll() {
    }

    @Test
    fun update() {
    }
}