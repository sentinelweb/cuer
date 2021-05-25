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
        every { timeProvider.currentTimeMillis() } returns baseTime
    }

    @Test
    fun formatTimeSince_now() {
        assertEquals("now", sut.formatTimeSince(baseTime - 3000))
    }

    @Test
    fun formatTimeSince_future() {
        assertEquals("!", sut.formatTimeSince(baseTime + 3000))
    }

    @Test
    fun formatTimeSince_sec() {
        assertEquals("30s", sut.formatTimeSince(baseTime - 30 * 1000L))
    }

    @Test
    fun formatTimeSince_min() {
        assertEquals("20m", sut.formatTimeSince(baseTime - 1000L * 60 * 20))
    }

    @Test
    fun formatTimeSince_hrs() {
        assertEquals("20h", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 20))
    }

    @Test
    fun formatTimeSince_days() {
        assertEquals("200d", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 24 * 200))
    }

    @Test
    fun formatTimeSince_yrs() {
        assertEquals("2y", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 24 * 365 * 2))
    }

    @Test
    fun formatTimeSince_too_long() {
        assertEquals("-", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 24 * 365 * 21))
    }

}