package uk.co.sentinelweb.cuer.domain

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.ext.deserialiseMedia
import uk.co.sentinelweb.cuer.domain.ext.serialise

class MediaDomainTest_Serialization {
    private val fixture = kotlinFixture {
        nullabilityStrategy(NeverNullStrategy)
        repeatCount { 6 }
        // repeatCount(PlaylistDomain::items) { 7 }
    }

    //@Fixture
    private lateinit var media: MediaDomain

    @Before
    fun setUp() {
        //FixtureAnnotations.initFixtures(this)
        media = fixture()
    }

    @Test
    fun serialization() {
        val serialized = media.serialise()
        val deserializedMedia = deserialiseMedia(serialized)
        assertEquals(media, deserializedMedia)
    }
}