package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.domain.LocalNodeDomain

fun Pair<String, Int>.http() = "http://$first:$second"
fun LocalNodeDomain.http() = "http://$ipAddress:$port"

fun Pair<String, Int>.https() = "https://$first:$second"
fun LocalNodeDomain.https() = "https://$ipAddress:$port"