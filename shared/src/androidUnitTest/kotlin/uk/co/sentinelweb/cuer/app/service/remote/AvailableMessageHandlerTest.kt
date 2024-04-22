package uk.co.sentinelweb.cuer.app.service.remote

import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.sentinelweb.cuer.app.util.wrapper.VibrateWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.remote.RemoteStatusInteractor
import uk.co.sentinelweb.cuer.remote.server.AvailableMessageMapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage.MsgType.JoinReply
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage.MsgType.PingReply
import uk.co.sentinelweb.cuer.tools.rule.CoroutineTestRule

class AvailableMessageHandlerTest {
    @get:Rule
    var rule = CoroutineTestRule()

    private lateinit var sut: AvailableMessageHandler

    private val remotesRepository: RemotesRepository = mockk(relaxed = true)


    private val remoteStatusInteractor: RemoteStatusInteractor = mockk(relaxed = true)

    private val availableMessageMapper: AvailableMessageMapper = mockk(relaxed = true)
    private val localRepository: LocalRepository = mockk(relaxed = true)
    private val wifiStateProvider: WifiStateProvider = mockk(relaxed = true)
    private val vibrateWrapper: VibrateWrapper = mockk(relaxed = true)

    private val fixtWifiState: WifiStateProvider.WifiState = WifiStateProvider.WifiState()
    private val fixtRemoteNode: RemoteNodeDomain =
        RemoteNodeDomain(id = null, ipAddress = "ipAddressRemote", port = 1234)
    private val fixtDeviceInfo = AvailableMessage.DeviceInfo(
        id = null,
        ipAddress = "ipAddressDevice",
        port = 2345,
        hostname = "hostname",
        device = "device",
        deviceType = NodeDomain.DeviceType.OTHER,
        authType = AvailableMessage.AuthMethod.Open,
        version = "",
        versionCode = 0,
    )

    @Before
    fun setUp() {
        sut = AvailableMessageHandler(
            remotesRepository,
            availableMessageMapper,
            remoteStatusInteractor,
            localRepository,
            wifiStateProvider,
            vibrateWrapper,
            SystemLogWrapper()
        )
    }


    @Test
    fun `messageReceived ping should send ping reply`() = runTest {
        val fixtAvailableMessage = AvailableMessage(AvailableMessage.MsgType.Ping, fixtDeviceInfo)
        val fixRemoteNodeAvailable = fixtRemoteNode.copy(isAvailable = true)
        every {
            availableMessageMapper.mapFromMulticastMessage(
                fixtAvailableMessage.node,
                fixtWifiState
            )
        } returns fixtRemoteNode

        val slot = slot<RemoteNodeDomain>()
        coEvery { remotesRepository.addUpdateNode(capture(slot)) } returns Unit
        every { wifiStateProvider.wifiState } returns fixtWifiState

        sut.messageReceived(fixtAvailableMessage)

        assertTrue(slot.captured.isAvailable)
        coVerify { remotesRepository.addUpdateNode(fixRemoteNodeAvailable) }
        coVerify { remoteStatusInteractor.available(PingReply, fixRemoteNodeAvailable) }
        coVerify { vibrateWrapper.vibrate() }
    }

    @Test
    fun `messageReceived Join should send JoinReply`() = runTest {
        val fixtAvailableMessage = AvailableMessage(AvailableMessage.MsgType.Join, fixtDeviceInfo)
        val fixRemoteNodeAvailable = fixtRemoteNode.copy(isAvailable = true)
        every {
            availableMessageMapper.mapFromMulticastMessage(
                fixtAvailableMessage.node,
                fixtWifiState
            )
        } returns fixtRemoteNode
        val slot = slot<RemoteNodeDomain>()
        coEvery { remotesRepository.addUpdateNode(capture(slot)) } returns Unit
        every { wifiStateProvider.wifiState } returns fixtWifiState

        sut.messageReceived(fixtAvailableMessage)

        assertTrue(slot.captured.isAvailable)
        coVerify { remotesRepository.addUpdateNode(fixRemoteNodeAvailable) }
        coVerify { remoteStatusInteractor.available(JoinReply, fixRemoteNodeAvailable) }
        coVerify { vibrateWrapper.vibrate() }
    }

    @Test
    fun `messageReceived PingReply should NOT send`() = runTest {
        val fixtAvailableMessage = AvailableMessage(AvailableMessage.MsgType.PingReply, fixtDeviceInfo)
        val fixRemoteNodeAvailable = fixtRemoteNode.copy(isAvailable = true)
        every {
            availableMessageMapper.mapFromMulticastMessage(
                fixtAvailableMessage.node,
                fixtWifiState
            )
        } returns fixtRemoteNode
        val slot = slot<RemoteNodeDomain>()
        coEvery { remotesRepository.addUpdateNode(capture(slot)) } returns Unit
        every { wifiStateProvider.wifiState } returns fixtWifiState

        sut.messageReceived(fixtAvailableMessage)

        assertTrue(slot.captured.isAvailable)
        coVerify { remotesRepository.addUpdateNode(fixRemoteNodeAvailable) }
        coVerify(exactly = 0) { remoteStatusInteractor.available(any(), any()) }
        coVerify(exactly = 0) { vibrateWrapper.vibrate() }
    }

    @Test
    fun `messageReceived JoinReply should NOT send`() = runTest {
        val fixtAvailableMessage = AvailableMessage(AvailableMessage.MsgType.JoinReply, fixtDeviceInfo)
        val fixRemoteNodeAvailable = fixtRemoteNode.copy(isAvailable = true)
        every {
            availableMessageMapper.mapFromMulticastMessage(
                fixtAvailableMessage.node,
                fixtWifiState
            )
        } returns fixtRemoteNode

        val slot = slot<RemoteNodeDomain>()
        coEvery { remotesRepository.addUpdateNode(capture(slot)) } returns Unit
        every { wifiStateProvider.wifiState } returns fixtWifiState

        sut.messageReceived(fixtAvailableMessage)

        assertTrue(slot.captured.isAvailable)
        coVerify { remotesRepository.addUpdateNode(fixRemoteNodeAvailable) }
        coVerify(exactly = 0) { remoteStatusInteractor.available(any(), any()) }
        coVerify(exactly = 0) { vibrateWrapper.vibrate() }
    }

    @Test
    fun `messageReceived Close should NOT send`() = runTest {
        val fixtAvailableMessage = AvailableMessage(AvailableMessage.MsgType.Close, fixtDeviceInfo)
        val fixRemoteNodeNotAvailable = fixtRemoteNode.copy(isAvailable = false)
        every {
            availableMessageMapper.mapFromMulticastMessage(
                fixtAvailableMessage.node,
                fixtWifiState
            )
        } returns fixtRemoteNode
        val slot = slot<RemoteNodeDomain>()
        coEvery { remotesRepository.addUpdateNode(capture(slot)) } returns Unit
        every { wifiStateProvider.wifiState } returns fixtWifiState

        sut.messageReceived(fixtAvailableMessage)

        assertFalse(slot.captured.isAvailable)
        coVerify { remotesRepository.addUpdateNode(fixRemoteNodeNotAvailable) }
        coVerify(exactly = 0) { remoteStatusInteractor.available(any(), any()) }
        coVerify(exactly = 0) { vibrateWrapper.vibrate() }
    }

    @After
    fun tearDown() {
    }
}