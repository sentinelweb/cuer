package uk.co.sentinelweb.cuer.app.db.typeconverter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

class InstantTypeConverterTest {

    private val fixtInstant = Instant.now()
    private val sut = InstantTypeConverter()
    @Before
    fun setUp() {
    }

    @Test
    fun toDb() {
        val actual = sut.toDb(fixtInstant)

        assertEquals(fixtInstant.toString(), actual)
    }

    @Test
    fun toDb_null() {
        val actual = sut.toDb(null)

        assertEquals("null", actual)
    }

    @Test
    fun fromDb() {
        val actual = sut.fromDb(fixtInstant.toString())

        assertEquals(fixtInstant, actual)
    }

    @Test
    fun fromDb_null() {
        val actual = sut.fromDb("null")

        assertNull(actual)
    }
}