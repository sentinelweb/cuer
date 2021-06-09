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
        assertEquals("20d", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 24 * 20))
    }

    @Test
    fun formatTimeSince_months() {
        assertEquals("6mth", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 24 * 200))
    }

    @Test
    fun formatTimeSince_yrs() {
        assertEquals("2y", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 24 * 365 * 2))
    }

    @Test
    fun formatTimeSince_too_long() {
        assertEquals("-", sut.formatTimeSince(baseTime - 1000L * 60 * 60 * 24 * 365 * 21))
    }

    //////////////////// short ////////////////////////////////
    @Test
    fun formatTimeShort_sec() {
        assertEquals("30s", sut.formatTimeShort(30 * 1000L))
    }

    @Test
    fun formatTimeShort_min() {
        assertEquals("20m", sut.formatTimeShort(1000L * 60 * 20))
    }

    @Test
    fun formatTimeShort_hrs() {
        assertEquals("20h", sut.formatTimeShort(1000L * 60 * 60 * 20))
    }

    @Test
    fun formatTimeShort_days() {
        assertEquals("20d", sut.formatTimeShort(1000L * 60 * 60 * 24 * 20))
    }

    @Test
    fun formatTimeShort_months() {
        assertEquals("6mth", sut.formatTimeShort(1000L * 60 * 60 * 24 * 200))
    }

    @Test
    fun formatTimeShort_yrs() {
        assertEquals("2y", sut.formatTimeShort(1000L * 60 * 60 * 24 * 365 * 2))
    }

    @Test
    fun formatTimeShort_too_long() {
        assertEquals("-", sut.formatTimeShort(1000L * 60 * 60 * 24 * 365 * 21))
    }

}