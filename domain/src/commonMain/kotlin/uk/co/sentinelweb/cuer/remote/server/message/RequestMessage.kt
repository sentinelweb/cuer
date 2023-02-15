package uk.co.sentinelweb.cuer.remote.server.message

import kotlinx.serialization.Serializable

@Serializable
data class RequestMessage constructor(
    val payload: Message
) : Message