package uk.co.sentinelweb.cuer.app.orchestrator

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator

class IdentifierTest {

    val guidCreator = GuidCreator()

    @Before
    fun setUp() {
    }

    @Test
    fun `isEqual LOCAL_NETWORK`() {
        val id1 = guidCreator.create().toLocalNetworkIdentifier(Locator("1.2.3.4", 1234))
        val id2 = guidCreator.create().toLocalNetworkIdentifier(Locator("1.2.3.4", 1234))

        assertNotEquals(id1, id2) // different guids

        assertEquals(id1, id1.id.toLocalNetworkIdentifier(id1.locator!!)) // same guid

        val id3 = id1.id.toIdentifier(LOCAL)
        assertNotEquals(id1, id3) // same guiud, different source, no locator
    }

    @After
    fun tearDown() {
    }
}