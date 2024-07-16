package uk.co.sentinelweb.cuer.net.remote

import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import uk.co.sentinelweb.cuer.net.client.ServiceExecutor
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerMessage.PlayPause
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerMessage.SeekToFraction

@InternalCoroutinesApi
class RemotePlayerServiceTest {

    @MockK
    internal lateinit var executor: ServiceExecutor

    internal lateinit var sut: RemotePlayerService

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        sut = RemotePlayerService(executor)
    }

    @Test
    fun `executeCommand with PlayPause message`() = runTest {
        val fixtLocator = Locator("xxx", 222)
        val fixtIsPlaying = false
        val fixtMessage = PlayPause(fixtIsPlaying)
        val fixtResponse = ResponseDomain()
        coEvery { executor.getResponse(any()) } returns fixtResponse

        sut.executeCommand(fixtLocator, fixtMessage)

        val pathSlot = slot<String>()
        coVerify {
            executor.getResponse(path = capture(pathSlot))
        }
        assertEquals("xxx:222/player/command/PlayPause/$fixtIsPlaying", pathSlot.captured)
        confirmVerified(executor)
    }

    @Test
    fun `executeCommand with SeekToFraction message`() = runTest {
        val fixtLocator = Locator("xxx", 222)
        val fixtFraction = 0.5f
        val fixtMessage = SeekToFraction(fixtFraction)
        val fixtResponse = ResponseDomain()
        coEvery { executor.getResponse(any()) } returns fixtResponse

        sut.executeCommand(fixtLocator, fixtMessage)

        val pathSlot = slot<String>()
        coVerify {
            executor.getResponse(path = capture(pathSlot))
        }
        assertEquals("xxx:222/player/command/SeekToFraction/$fixtFraction", pathSlot.captured)
        confirmVerified(executor)
    }

    fun `executeCommand with TrackSelected message`() = runTest {
        val fixtLocator = Locator("xxx", 222)
        val guid = "guid-xx-xx-xx"
        val fixtId = OrchestratorContract.Identifier(GUID(guid), MEMORY)
        val fixtMessage = PlayerSessionContract.PlayerMessage.TrackSelected(fixtId, false)
        val fixtResponse = ResponseDomain()
        coEvery { executor.getResponse(any()) } returns fixtResponse

        sut.executeCommand(fixtLocator, fixtMessage)

        val pathSlot = slot<String>()
        coVerify {
            executor.getResponse(path = capture(pathSlot))
        }
        assertEquals("xxx:222/player/command/TrackSelected/$guid", pathSlot.captured)
        confirmVerified(executor)
    }
}
