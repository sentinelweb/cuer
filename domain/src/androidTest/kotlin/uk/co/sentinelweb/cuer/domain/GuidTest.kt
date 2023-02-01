package uk.co.sentinelweb.cuer.domain

import com.appmattus.kotlinfixture.decorator.nullability.NeverNullStrategy
import com.appmattus.kotlinfixture.decorator.nullability.nullabilityStrategy
import com.appmattus.kotlinfixture.kotlinFixture
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import kotlin.test.assertNotNull

class GuidTest {

    private val fixture = kotlinFixture {
        nullabilityStrategy(NeverNullStrategy)
        repeatCount { 6 }
        factory { Identifier(GuidCreator().create(), fixture()) }
        // repeatCount(PlaylistDomain::items) { 7 }
    }

    @Test
    fun serialization() {
        val guid = fixture<GUID>()
        assertNotNull(guid)
        val guidIdentifier = fixture<Identifier<GUID>>()
        assertNotNull(guidIdentifier)
        val playlist = fixture<PlaylistDomain>()
        assertNotNull(playlist)
    }
}