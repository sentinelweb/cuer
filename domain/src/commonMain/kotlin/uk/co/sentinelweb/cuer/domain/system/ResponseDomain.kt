package uk.co.sentinelweb.cuer.domain.system

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.Domain

@Serializable
data class ResponseDomain constructor(
    val payload: List<Domain> = listOf(),
    val errors: List<ErrorDomain> = listOf()
) {
    constructor(
        payload: Domain?,
        errors: List<ErrorDomain> = listOf()
    ) : this(payload?.let { listOf(it) } ?: listOf(), errors)

    constructor(
        error: ErrorDomain
    ) : this(errors = listOf(error))
}

