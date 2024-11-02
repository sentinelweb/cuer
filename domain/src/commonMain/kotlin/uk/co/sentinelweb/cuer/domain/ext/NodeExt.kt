package uk.co.sentinelweb.cuer.domain.ext

import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

fun LocalNodeDomain.name() = this.hostname ?: this.ipAddress
fun RemoteNodeDomain.name() = this.hostname ?: this.ipAddress
fun NodeDomain.name() = when(this) {
    is LocalNodeDomain -> this.name()
    is RemoteNodeDomain -> this.name()
    else -> "Invalid node: $this"
}
fun NodeDomain.isAvailable() = when(this) {
    is LocalNodeDomain -> true
    is RemoteNodeDomain -> isAvailable
    else -> error("Invalid node: $this")
}

