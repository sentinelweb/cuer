package uk.co.sentinelweb.cuer.domain.system

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.domain.ext.deserialiseResponse
import uk.co.sentinelweb.cuer.domain.ext.serialise

class ResponseDomainTest_Serialization {
    private val fixture = kotlinFixture {
        nullabilityStrategy(NeverNullStrategy)
        repeatCount { 6 }
        factory { OrchestratorContract.Identifier(GuidCreator().create(), fixture()) }
    }

    // @Fixture
    private lateinit var playlist: PlaylistDomain

    @Before
    fun setUp() {
        // FixtureAnnotations.initFixtures(this)
        playlist = fixture()
    }

    @Test
    fun serialization() {
        val response = ResponseDomain(playlist)
        val serializedResponse = response.serialise()
        val deserializedResponse = deserialiseResponse(serializedResponse)
        assertEquals(playlist, deserializedResponse.payload[0] as PlaylistDomain)
    }
}
