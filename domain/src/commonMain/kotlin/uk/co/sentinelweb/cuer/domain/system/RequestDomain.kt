package uk.co.sentinelweb.cuer.domain.system

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.Domain

@Serializable
data class RequestDomain constructor(
    val payload: Domain
)