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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Operation.FULL
import uk.co.sentinelweb.cuer.domain.MediaDomain
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
    val sut: MediaDatabaseRepository by inject()
    val captureMediaFlow: MutableList<Pair<OrchestratorContract.Operation,MediaDomain>> = mutableListOf()

    @Before
    fun before(){
        Database.Schema.create(get())
    }


    @Test
    fun saveCreate() = runTest {
        val initial = fixture<MediaDomain>()

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
        sut.updates.test {
            assertEquals(FULL to saved, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun saveList() {

    }

    fun saveChannelExists() = runTest {
    }

    @Test
    fun testSave() = runTest {
    }

    @Test
    fun load() = runTest {

    }

    @Test
    fun loadList() = runTest {
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