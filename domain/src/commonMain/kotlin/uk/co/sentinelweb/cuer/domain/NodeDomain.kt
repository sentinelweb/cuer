package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract

@Serializable
data class NodeDomain(
    val id: OrchestratorContract.Identifier<GUID>,
    val ipAddress: String,
    val port: Int,
    val hostname: String? = null,
    val device: String? = null,
    val username: String? = null,
    val password: String? = null,
    val token: String? = null,
) : Domain