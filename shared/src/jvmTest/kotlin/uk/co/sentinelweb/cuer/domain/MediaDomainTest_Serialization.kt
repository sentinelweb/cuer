package uk.co.sentinelweb.cuer.domain

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.annotations.Fixture
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.ext.deserialiseMedia
import uk.co.sentinelweb.cuer.domain.ext.serialise

class MediaDomainTest_Serialization {

    @Fixture
    private lateinit var media: MediaDomain

    @Before
    fun setUp() {
        FixtureAnnotations.initFixtures(this)
    }

    @Test
    fun serialization() {
        val serialized = media.serialise()
        val deserializedMedia = deserialiseMedia(serialized)
        assertEquals(media, deserializedMedia)
    }
}