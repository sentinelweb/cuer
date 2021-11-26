package uk.co.sentinelweb.cuer.app.ui.player

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent.SeekToPosition
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.tools.rule.CoroutineTestRule

@ExperimentalCoroutinesApi
class PlayerListenerTest {
    @get:Rule
    var rule = CoroutineTestRule()

    private val testCoroutines = TestCoroutineContextProvider(rule.dispatcher)
    private lateinit var sut: PlayerListener

    @Before
    fun setUp() {
        sut = PlayerListener(testCoroutines, SystemLogWrapper())
    }

    @Test
    fun seekTo() = rule.dispatcher.runBlockingTest {
        sut.intentFlow.test {
            sut.seekTo(1000)
            assertThat(awaitItem()).isEqualTo(SeekToPosition(1000))
            cancelAndConsumeRemainingEvents()
        }
    }
}