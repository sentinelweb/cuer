package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier

@Serializable
data class ImageDomain constructor(
    val id: Identifier<GUID>? = null,
    val url: String,
    val width: Int? = null,
    val height: Int? = null
) : Domain