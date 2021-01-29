package uk.co.sentinelweb.cuer.core.mappers

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.core.providers.TimeProvider

class TimeSinceFormatterTest {

    private var timeProvider: TimeProvider = mockk(relaxUnitFun = true)

    private val sut = TimeSinceFormatter(timeProvider)

    private val baseTime = System.currentTimeMillis()

    @Before
    fun setUp() {
        // MockKAnnotations.init(relaxUnitFun = true)
        every { timeProvider.currentTimeMillis() } returns baseTime
    }

    @Test
    fun `formatTimeSince now`() {
        assertEquals("now", sut.formatTimeSince(baseTime - 3000))
    }

    @Test
    fun `formatTimeSince future`() {
        assertEquals("!", sut.formatTimeSince(baseTime + 3000))
    }

    @Test
    fun `formatTimeSince sec`() {
        assertEquals("30s", sut.formatTimeSince(baseTime - 30 * 1000L))
    }

    @Test
    fun `formatTimeSince min`() {
        assertEquals("20m", sut.formatTimeSince(baseTime - 1000L * 60 * 20))
    }

    @Test
    fun `formatTimeSince hrs`() {
        assertEquals("20h", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 20))
    }

    @Test
    fun `formatTimeSince days`() {
        assertEquals("200d", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 24 * 200))
    }

    @Test
    fun `formatTimeSince yrs`() {
        assertEquals("2y", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 24 * 365 * 2))
    }

    @Test
    fun `formatTimeSince too log`() {
        assertEquals("-", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 24 * 365 * 21))
    }

}