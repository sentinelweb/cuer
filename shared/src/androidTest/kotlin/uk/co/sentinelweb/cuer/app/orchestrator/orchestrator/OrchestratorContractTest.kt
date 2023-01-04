package uk.co.sentinelweb.cuer.app.orchestrator.orchestrator

import com.flextrade.jfixture.JFixture
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.tools.ext.build

class OrchestratorContractTest {
    private val fixture = JFixture()

    @Before
    fun setUp() {

    }

    @Test
    fun testIdentifiersEquivalent() {
        val id: Long = fixture.build()
        assertThat(Identifier(id, LOCAL)).isEqualTo(Identifier(id, LOCAL))
        assertThat(LocalIdentifier(id)).isEqualTo(Identifier(id, LOCAL))
        assertThat(MemoryIdentifier(id)).isEqualTo(Identifier(id, MEMORY))
    }

    @Test
    fun testIdentifiersEquivalentNotEquivalent() {
        val id: Long = fixture.build()
        assertThat(LocalIdentifier(id)).isNotEqualTo(MemoryIdentifier(id))
        assertThat(MemoryIdentifier(id)).isNotEqualTo(Identifier(id, LOCAL))
        assertThat(LocalIdentifier(id)).isNotEqualTo(Identifier(id, MEMORY))
    }
}