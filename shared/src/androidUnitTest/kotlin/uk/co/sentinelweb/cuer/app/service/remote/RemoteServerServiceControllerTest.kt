package uk.co.sentinelweb.cuer.app.service.remote

import app.cash.turbine.test
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract.Notification.External
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.remote.server.*
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage.MsgType.Join
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage.MsgType.Ping
import uk.co.sentinelweb.cuer.tools.rule.CoroutineTestRule

class RemoteServerServiceControllerUnitTest : AutoCloseKoinTest() {
    @get:Rule
    var rule = CoroutineTestRule()

    private val controller: RemoteServerContract.Controller by inject()

    private val testCoroutines = TestCoroutineContextProvider(rule.dispatcher)

    @Before
    fun setup() {
        startKoin {
            modules(
                module {
                    single<RemoteServerContract.Controller> {
                        RemoteServerServiceController(
                            get(),
                            get(),
                            get(),
                            get(),
                            get(),
                            get(),
                            get(),
                            get(),
                            get(),
                            get()
                        )
                    }
                    single { mockk<External>(relaxed = true) }
                    single { mockk<RemoteWebServerContract>(relaxed = true) }
                    single { mockk<MultiCastSocketContract>(relaxed = true) }
                    single<CoroutineContextProvider> { testCoroutines }
                    single<LogWrapper> { SystemLogWrapper() }
                    single { mockk<RemotesRepository>(relaxed = true) }
                    single { mockk<LocalRepository>(relaxed = true) }
                    single { mockk<WakeLockManager>(relaxed = true) }
                    single { mockk<WifiStateProvider>(relaxed = true) }
                    single { mockk<RemoteServerContract.Service>(relaxed = true) }
                }
            )
        }

    }

    @Test
    fun `controller isServerStarted`() = runTest {
        every { get<RemoteWebServerContract>().isRunning } returns true

        val actual = controller.isServerStarted

        assertTrue(actual)
    }

    @Test
    fun `controller initialise()`() = runTest {
        val fixtPort = 1234
        every { get<RemoteWebServerContract>().isRunning } returns true
        every { get<RemoteWebServerContract>().port } returns fixtPort
        val fixtWifiState = WifiStateProvider.WifiState(
            isConnected = true,
            ssid = "test_ssid",
            ip = "test_ip",
            isObscured = false
        )
        every { get<WifiStateProvider>().wifiState } returns fixtWifiState

        val slot = slot<() -> Unit>()
        every { get<RemoteWebServerContract>().start(capture(slot)) } answers { slot.captured.invoke() }
        val slotMulti = slot<() -> Unit>()
        coEvery { get<MultiCastSocketContract>().runSocketListener(capture(slotMulti)) } answers { slotMulti.captured.invoke() }

        controller.initialise()
        verify { get<External>().updateNotification("Starting server...") }
        verify { get<RemoteWebServerContract>().start(any()) }
        verify {
            val pair = fixtWifiState.ip!! to fixtPort
            get<External>().updateNotification(pair.http())
        }
        coVerify { get<MultiCastSocketContract>().runSocketListener(any()) }
        coVerify { get<WifiStateProvider>().wifiStateFlow }
        verify { get<WakeLockManager>().acquireWakeLock() }

        advanceTimeBy(300)
        coVerify { get<MultiCastSocketContract>().send(Join) }
    }

    @Test
    fun `controller test auto stop()`() = runTest {
        val fixtWifiState = WifiStateProvider.WifiState(
            isConnected = false,
            ssid = null,
            ip = null,
            isObscured = true
        )
        every { get<WifiStateProvider>().wifiState } returns fixtWifiState
        val testFlow = MutableStateFlow(fixtWifiState)
        every { get<WifiStateProvider>().wifiStateFlow } returns testFlow

        controller.initialise()

        testFlow.test {
            awaitItem()
            coVerify { get<RemoteServerContract.Service>().stopSelf() }
        }
    }

    @Test
    fun `multicastPing calls multi send with Ping`() = runTest {
        coEvery { get<MultiCastSocketContract>().send(Ping) } returns Unit

        controller.multicastPing()

        coVerify {  get<MultiCastSocketContract>().send(Ping) }
    }

    @Test
    fun `handleAction passes to notification`() = runTest {
        val fixtAction = "testAction"
        controller.handleAction(fixtAction)
        verify {  get<External>().handleAction(fixtAction) }
    }

    @Test
    fun `controller destroy`() = runTest {

        controller.destroy()

        coVerify {  get<WakeLockManager>().releaseWakeLock() }
        coVerify {  get<RemoteWebServerContract>().stop() }
        coVerify {  get<External>().destroy() }
        coVerify {  get<RemotesRepository>().setUnAvailable() }

        advanceTimeBy(50)
        coVerify {  get<MultiCastSocketContract>().close() }
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}