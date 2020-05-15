package uk.co.sentinelweb.cuer.core.mappers

import org.junit.Before
import org.junit.Test

class DateTimeMapperTest {
    private val sut = DateTimeMapper()

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
        sut.mapTimestamp("2020-04-06T16:00:16.0Z")
        sut.mapTimestamp("2020-04-06T16:00:16.00Z")
        sut.mapTimestamp("2020-04-06T16:00:16.000Z")

    }


    @Test
    fun mapDuration() {
        sut.mapDuration("PT1S")
        sut.mapDuration("PT1M1S")
        sut.mapDuration("PT1H1M1S")
    }

    @Test
    fun formatTime() {
        val actual = sut.formatTime(1000 * 60 * 60 * 18)
        //assertEqual(actual, "18:00:00")
    }
}