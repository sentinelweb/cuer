package uk.co.sentinelweb.cuer.domain.ext

import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
import kotlin.random.Random

class FlagsExtKtTest {

    @Before
    fun setUp() {
    }

    @Test
    fun hasFlag() {
        val testWatched = Random.nextLong() or FLAG_WATCHED
        assertTrue(testWatched.hasFlag(FLAG_WATCHED))

        val testNotStarred = Random.nextLong() and FLAG_STARRED.inv()
        assertFalse(testNotStarred.hasFlag(FLAG_STARRED))
    }


    @Test
    fun setFlag() {
        val testOn = Random.nextLong().setFlag(FLAG_WATCHED, true)
        assertTrue(testOn.hasFlag(FLAG_WATCHED))
        val testOff = testOn.setFlag(FLAG_WATCHED, false)
        assertFalse(testOff.hasFlag(FLAG_WATCHED))
    }

}