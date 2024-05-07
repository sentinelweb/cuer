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
    private val playControls: MediaSessionMessageListener = mockk(relaxed = true)
    private val log: LogWrapper = mockk(relaxed = true)
    private val store: PlayerContract.MviStore = mockk(relaxed = true)
    private val testCoroutines = TestCoroutineContextProvider(rule.dispatcher)

    private lateinit var sut: PlayerController

    @Before
    fun setUp() {
        sut = PlayerController(
            queueConsumer, modelMapper, testCoroutines,
            playControls, log, store, null
        )
    }

    @Test
    fun onViewCreated() = runTest {
        val slot = slot<PlayerContract.MviStore.Intent>()
        val mutableSharedFlow = MutableSharedFlow<PlayerContract.MviStore.Intent>()
        coEvery { playControls.intentFlow } returns mutableSharedFlow
        sut.onViewCreated(listOf())
        sut.onStart()
        val expected = SeekToPosition(1000)
        mutableSharedFlow.emit(expected)
        coVerify { store.accept(capture(slot)) }
        Truth.assertThat(slot.captured).isEqualTo(expected)
    }
}