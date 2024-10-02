package uk.co.sentinelweb.cuer.domain.ext

import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain

fun RemoteNodeDomain.name() = this.hostname ?: this.ipAddress