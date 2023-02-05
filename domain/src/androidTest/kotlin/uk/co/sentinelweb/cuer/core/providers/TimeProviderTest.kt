package uk.co.sentinelweb.cuer.core.providers

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class TimeProviderTest {

    private val sut: TimeProvider = TimeProvider()

    @Before
    fun setUp() {
    }

    @Test
    @Ignore("not time synced")
    fun instant() {
        assertEquals(sut.instant(), Clock.System.now())
    }

    @Test
    @Ignore("not time synced")
    fun localDateTime() {
        assertEquals(sut.localDateTime(), Clock.System.now().toLocalDateTime(TimeZone.UTC))
    }

    @Test
    @Ignore("not time synced")
    fun currentTimeMills() {
        assertEquals(sut.currentTimeMillis(), Clock.System.now().toEpochMilliseconds())
    }

}