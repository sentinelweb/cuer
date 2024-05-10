package uk.co.sentinelweb.cuer.app.ui.player

import com.google.common.truth.Truth
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.service.remote.player.PlayerSessionListener
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent.SeekToPosition
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.tools.rule.CoroutineTestRule

@ExperimentalCoroutinesApi
class PlayerControllerTest {
    @get:Rule
    var rule = CoroutineTestRule()

    private val queueConsumer: QueueMediatorContract.Consumer = mockk(relaxed = true)
    private val modelMapper: PlayerModelMapper = mockk(relaxed = true)
    private val mediaSessionListener: MediaSessionListener = mockk(relaxed = true)
    private val log: LogWrapper = mockk(relaxed = true)
    private val store: PlayerContract.MviStore = mockk(relaxed = true)
    private val testCoroutines = TestCoroutineContextProvider(rule.dispatcher)
    private val playerSessionListener: PlayerSessionListener = mockk(relaxed = true)

    private lateinit var sut: PlayerController

    @Before
    fun setUp() {
        sut = PlayerController(
            queueConsumer = queueConsumer,
            modelMapper = modelMapper,
            coroutines = testCoroutines,
            mediaSessionListener = mediaSessionListener,
            playSessionListener = playerSessionListener,
            log = log,
            store = store,
            lifecycle = null,
        )
    }

    @Test
    fun onViewCreated_mediaSessionFlow() = runTest {
        val slot = slot<PlayerContract.MviStore.Intent>()
        val mediaMutableSharedFlow = MutableSharedFlow<PlayerContract.MviStore.Intent>()
        coEvery { mediaSessionListener.intentFlow } returns mediaMutableSharedFlow
        sut.onViewCreated(listOf())
        sut.onStart()
        val expected = SeekToPosition(1000)
        mediaMutableSharedFlow.emit(expected)
        coVerify { store.accept(capture(slot)) }
        Truth.assertThat(slot.captured).isEqualTo(expected)
    }

    @Test
    fun onViewCreated_playerSessionFlow() = runTest {
        val slot = slot<PlayerContract.MviStore.Intent>()
        val playerMutableSharedFlow = MutableSharedFlow<PlayerContract.MviStore.Intent>()
        coEvery { playerSessionListener.intentFlow } returns playerMutableSharedFlow
        sut.onViewCreated(listOf())
        sut.onStart()
        val expected = SeekToPosition(1000)
        playerMutableSharedFlow.emit(expected)
        coVerify { store.accept(capture(slot)) }
        Truth.assertThat(slot.captured).isEqualTo(expected)
    }
}