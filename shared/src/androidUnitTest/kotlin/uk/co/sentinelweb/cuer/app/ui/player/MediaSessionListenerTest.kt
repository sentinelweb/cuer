package uk.co.sentinelweb.cuer.app.ui.player

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Intent.SeekToPosition
import uk.co.sentinelweb.cuer.core.providers.TestCoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.tools.rule.CoroutineTestRule

//todo something wrong with test setup
@ExperimentalCoroutinesApi
class MediaSessionListenerTest {
    @get:Rule
    var rule = CoroutineTestRule()

    private val testCoroutines = TestCoroutineContextProvider(rule.dispatcher)
    private lateinit var sut: MediaSessionListener

    @Before
    fun setUp() {
        sut = MediaSessionListener(testCoroutines, SystemLogWrapper())
    }

    @Test
    fun seekTo() = runTest {
        sut.intentFlow.test {
            sut.seekTo(1000)
            assertThat(awaitItem()).isEqualTo(SeekToPosition(1000))
            cancelAndConsumeRemainingEvents()
        }
    }
}