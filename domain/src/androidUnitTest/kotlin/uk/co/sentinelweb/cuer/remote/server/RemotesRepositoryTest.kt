package uk.co.sentinelweb.cuer.remote.server

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.sentinelweb.cuer.app.db.repository.file.FileInteractor
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.tools.ext.kotlinFixtureDefaultConfig
import uk.co.sentinelweb.cuer.tools.rule.MainCoroutineRule

class RemotesRepositoryTest {

    private val fixture = kotlinFixtureDefaultConfig

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val localNode: LocalNodeDomain = fixture()

    private val fileInteractor = mockk<FileInteractor>(relaxed = true)
    private val localNodeRepo = mockk<LocalRepository>(relaxed = true) {
        coEvery { getLocalNode() } returns localNode
    }
    private val coroutines = TestCoroutineContextProvider(mainCoroutineRule.testDispatcher)
    private val log: LogWrapper = SystemLogWrapper()

    private lateinit var sut: RemotesRepository

    @Before
    fun setUp() {
    }

    @Test
    fun loadAll() = runTest {
        val initialNodes = remoteNodeDomains()
        coEvery { fileInteractor.loadJson() } returns initialNodes.serialise()
        sut = RemotesRepository(fileInteractor, localNodeRepo, coroutines, log)
        sut.updatesFlow.test {
            assertEquals(initialNodes, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addUpdateNode() = runTest {
        val initialNodes = remoteNodeDomains()
        coEvery { fileInteractor.loadJson() } returns initialNodes.serialise()
        sut = RemotesRepository(fileInteractor, localNodeRepo, coroutines, log)

        sut.updatesFlow.test {
            assertEquals(initialNodes, awaitItem())
            val updated = initialNodes[0].copy(isAvailable = true)
            sut.addUpdateNode(updated)
            coVerify { fileInteractor.saveJson(any()) }
            val updatedList = awaitItem()
            // assert the first item is the updated one
            assertEquals(updated, updatedList[0])
            // assert the rest of the list is the same
            assertEquals(initialNodes.subList(1, 5), updatedList.subList(1, 5))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removeNode() = runTest {
        val initialNodes = remoteNodeDomains()
        coEvery { fileInteractor.loadJson() } returns initialNodes.serialise()
        sut = RemotesRepository(fileInteractor, localNodeRepo, coroutines, log)

        sut.updatesFlow.test {
            assertEquals(initialNodes, awaitItem())
            val toRemove = initialNodes[0]
            sut.removeNode(toRemove)
            coVerify { fileInteractor.saveJson(any()) }
            val updatedList = awaitItem()
            assertEquals(5, updatedList.size)
            assertEquals(initialNodes.subList(1, 5), updatedList.subList(0, 4))
            //assertEquals(updated, awaitItem()[0])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setDisconnected() = runTest {
        val initialNodes = remoteNodeDomains()
        coEvery { fileInteractor.loadJson() } returns initialNodes.serialise()
        sut = RemotesRepository(fileInteractor, localNodeRepo, coroutines, log)

        sut.updatesFlow.test {
            assertEquals(initialNodes, awaitItem())

            initialNodes.forEach { initialNode ->
                val nodeUpdated = initialNode.copy(isAvailable = true)
                sut.addUpdateNode(nodeUpdated)
                assertTrue(awaitItem().filter { it.id == nodeUpdated.id }.first().isAvailable)
            }
            sut.setUnAvailable()

            assertEquals(initialNodes, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun remoteNodeDomains(): List<RemoteNodeDomain> {
        val initialNodes = fixture<List<RemoteNodeDomain>>()
            .mapIndexed { i, it -> it.copy(isAvailable = false, hostname = "host$i", ipAddress = "x.x.x.$i") }
        return initialNodes
    }
}