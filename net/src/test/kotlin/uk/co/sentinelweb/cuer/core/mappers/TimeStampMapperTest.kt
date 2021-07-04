package uk.co.sentinelweb.cuer.core.mappers

import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.net.mappers.TimeStampMapper

class TimeStampMapperTest {
    private val spyLog = spyk<LogWrapper>(SystemLogWrapper())
    private val sut = TimeStampMapper(spyLog)

    @Before
    fun setUp() {
    }

    @Test
    fun mapTimestamp() {
        // 2020-04-06T16:00:16Z
        // 2020-04-06T16:00:16.0Z
        // 2020-04-06T16:00:16.00Z
        // 2020-04-06T16:00:16.000Z
        sut.mapTimestamp("2020-04-06T16:00:16Z")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapTimestamp("2020-04-06T16:00:16.1Z")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapTimestamp("2020-04-06T16:00:16.12Z")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapTimestamp("2020-04-06T16:00:16.123Z")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapTimestamp("2020-04-06T16:00:16.1234Z")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapTimestamp("2020-04-06T16:00:16.12345Z")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapTimestamp("2020-04-06T16:00:16.123456Z")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapTimestamp("2020-04-06T16:00:16.1234567Z")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapTimestamp("2020-04-06T16:00:16.12345678Z")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapTimestamp("2020-04-06T16:00:16.123456789Z")
        verify(exactly = 0) { spyLog.e(any(), any()) }

    }

    @Test
    fun mapDuration() {
        sut.mapDuration("PT1S")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapDuration("PT1M1S")
        verify(exactly = 0) { spyLog.e(any(), any()) }
        sut.mapDuration("PT1H1M1S")
        verify(exactly = 0) { spyLog.e(any(), any()) }

        sut.mapDuration("XT1H1M1S") // prints error
        verify(exactly = 1) { spyLog.e(any(), any()) }
    }

    @Test
    fun mapDateTimeSimple() {
        sut.mapDateTimeSimple("2020-12-31_12:12:12")
        verify(exactly = 0) { spyLog.e(any(), any()) }
    }

}