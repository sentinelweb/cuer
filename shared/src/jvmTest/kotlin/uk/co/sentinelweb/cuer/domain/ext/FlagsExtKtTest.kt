package uk.co.sentinelweb.cuer.domain.ext

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_LIVE
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_LIVE_UPCOMING
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_PLAY_FROM_START
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_STARRED
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
import kotlin.random.Random

class FlagsExtKtTest {
    private val fixture = kotlinFixture { nullabilityStrategy(NeverNullStrategy) }

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

    @Test
    fun makeFlags() {
        val domain = fixture<MediaDomain>()
        val actual  = makeFlags(
            FLAG_WATCHED to domain.watched,
            FLAG_STARRED to domain.starred,
            FLAG_LIVE to domain.isLiveBroadcast,
            FLAG_LIVE_UPCOMING to domain.isLiveBroadcastUpcoming,
            FLAG_PLAY_FROM_START to domain.playFromStart
        )
        val expected = (if (domain.watched) FLAG_WATCHED else 0) +
            (if (domain.starred) FLAG_STARRED else 0) +
            (if (domain.isLiveBroadcast) FLAG_LIVE else 0) +
            (if (domain.isLiveBroadcastUpcoming) FLAG_LIVE_UPCOMING else 0) +
            (if (domain.playFromStart) FLAG_PLAY_FROM_START else 0)

        assertEquals("flags not correct $domain", expected, actual)
    }

}