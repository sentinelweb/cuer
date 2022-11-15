package uk.co.sentinelweb.cuer.core.providers

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime

class TimeProviderTest {

    private val sut: TimeProvider = TimeProvider()

    @Before
    fun setUp() {
    }

    @Test
    @Ignore
    fun instant() {
        assertEquals(sut.instant(), Instant.now())
    }

    @Test
    @Ignore
    fun localDateTime() {
        assertEquals(sut.localDateTime(), LocalDateTime.now())
    }

    @Test
    @Ignore
    fun currentTimeMills() {
        assertEquals(sut.currentTimeMillis(), System.currentTimeMillis())
    }

}