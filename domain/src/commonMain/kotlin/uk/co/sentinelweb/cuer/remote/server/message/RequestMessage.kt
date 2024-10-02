package uk.co.sentinelweb.cuer.remote.server.message

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.remote.server.Message

@Serializable
data class RequestMessage(
    val payload: Message
) : Message

@Serializable
data class ResponseMessage(
    val payload: Message
) : Message