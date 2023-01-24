package uk.co.sentinelweb.cuer.domain.system

import com.flextrade.jfixture.FixtureAnnotations
import com.flextrade.jfixture.annotations.Fixture
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseResponse
import uk.co.sentinelweb.cuer.domain.ext.serialise

class ResponseDomainTest_Serialization {

    @Fixture
    private lateinit var playlist: PlaylistDomain

    @Before
    fun setUp() {
        FixtureAnnotations.initFixtures(this)
    }

    @Test
    fun serialization() {
        val response = ResponseDomain(playlist)
        val serializedResponse = response.serialise()
        val deserializedResponse = deserialiseResponse(serializedResponse)
        assertEquals(playlist, deserializedResponse.payload[0] as PlaylistDomain)
    }
}