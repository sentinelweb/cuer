package uk.co.sentinelweb.cuer.app.util.cast.listener

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState.CC_CONNECTED
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState.CC_CONNECTING
import uk.co.sentinelweb.cuer.app.util.mediasession.MediaSessionManager
import uk.co.sentinelweb.cuer.app.util.wrapper.PhoenixWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextTestProvider

@ExperimentalCoroutinesApi
class ConnectionMonitorTest {
    @MockK
    private lateinit var mockToast: ToastWrapper

    @MockK
    private lateinit var mockCastWrapper: ChromeCastWrapper

    @MockK
    private lateinit var mockMediaSessionManager: MediaSessionManager

    @MockK
    private lateinit var mockPhoenixWrapper: PhoenixWrapper

    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val coCxtProvider: CoroutineContextProvider =
        CoroutineContextTestProvider(testCoroutineDispatcher)

    private lateinit var sut: ConnectionMonitor

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        sut = spyk(
            ConnectionMonitor(
                toast = mockToast,
                castWrapper = mockCastWrapper,
                mediaSessionManager = mockMediaSessionManager,
                phoenixWrapper = mockPhoenixWrapper,
                coCxtProvider = coCxtProvider
            )
        )
    }

    @Test
    fun `setTimer successful connect`() {
        val spyStateProvider: () -> CastPlayerContract.ConnectionState? = spyk({ CC_CONNECTED })
        runBlockingTest(testCoroutineDispatcher) {
            sut.setTimer(spyStateProvider)
            advanceTimeBy(10000)
        }
        verify {
            spyStateProvider()
            sut.setTimer(spyStateProvider)

        }
        verify(exactly = 0) { sut.emergencyCleanup() }
        confirmVerified(sut)
    }

    @Test
    fun `setTimer unsuccessful trigger cleanup`() {
        val spyStateProvider: () -> CastPlayerContract.ConnectionState? = spyk({ CC_CONNECTING })
        runBlockingTest(testCoroutineDispatcher) {
            sut.setTimer(spyStateProvider)
            advanceTimeBy(10000)
        }
        verify {
            spyStateProvider()
            sut.setTimer(spyStateProvider)

            sut.emergencyCleanup()
            mockPhoenixWrapper.triggerRestart()
            mockToast.show(any())
            mockCastWrapper.killCurrentSession()
            mockMediaSessionManager.destroyMediaSession()
        }
        confirmVerified(sut)
    }

    @Test
    fun `setTimer cancel`() {
        val spyStateProvider: () -> CastPlayerContract.ConnectionState? = spyk({ CC_CONNECTING })
        runBlockingTest(testCoroutineDispatcher) {
            sut.setTimer(spyStateProvider)
            advanceTimeBy(1000)
            sut.cancelTimer()
        }
        verify() {
            sut.setTimer(spyStateProvider)
            sut.cancelTimer()
        }
        verify(exactly = 0) {
            spyStateProvider()

            sut.emergencyCleanup()
            mockPhoenixWrapper.triggerRestart()
            mockToast.show(any())
            mockCastWrapper.killCurrentSession()
            mockMediaSessionManager.destroyMediaSession()
        }
        confirmVerified(sut)
    }

    @Test
    fun checkAlreadyConnected() {
        sut.checkAlreadyConnected(CC_CONNECTED)

        verify {
            sut.checkAlreadyConnected(CC_CONNECTED)

            sut.cancelTimer()
            sut.emergencyCleanup()
            mockPhoenixWrapper.triggerRestart()
            mockToast.show(any())
            mockCastWrapper.killCurrentSession()
            mockMediaSessionManager.destroyMediaSession()
        }
    }

    @Test
    fun emergencyCleanup() {
        sut.emergencyCleanup()
        verify() {
            sut.emergencyCleanup()

            mockPhoenixWrapper.triggerRestart()
            mockToast.show(any())
            mockCastWrapper.killCurrentSession()
            mockMediaSessionManager.destroyMediaSession()
        }
    }

}