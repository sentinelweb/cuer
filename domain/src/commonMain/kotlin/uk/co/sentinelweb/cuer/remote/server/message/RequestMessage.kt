package uk.co.sentinelweb.cuer.remote.server.message

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.remote.server.Message

@Serializable
data class RequestMessage constructor(
    val payload: Message
) : Message