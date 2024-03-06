package uk.co.sentinelweb.cuer.app.orchestrator.orchestrator

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator

class OrchestratorContractTest {

    @Before
    fun setUp() {

    }

    @Test
    fun testIdentifiersEquivalent() {
        val guid: GUID = GuidCreator().create()
        assertThat(Identifier(guid, LOCAL)).isEqualTo(Identifier(guid, LOCAL))
        assertThat(guid.toIdentifier(LOCAL)).isEqualTo(Identifier(guid, LOCAL))
        assertThat(guid.toIdentifier(MEMORY)).isEqualTo(Identifier(guid, MEMORY))
    }
}
