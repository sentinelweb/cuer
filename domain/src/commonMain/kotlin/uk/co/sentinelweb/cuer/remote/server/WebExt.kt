package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain


fun Pair<String, Int>.ipport() = "$first:$second"
fun LocalNodeDomain.ipport() = "$ipAddress:$port"
fun RemoteNodeDomain.ipport() = "$ipAddress:$port"

fun Pair<String, Int>.http() = "http://$first:$second"
fun LocalNodeDomain.http() = "http://$ipAddress:$port"
fun RemoteNodeDomain.http() = "http://$ipAddress:$port"

fun Pair<String, Int>.https() = "https://$first:$second"
fun LocalNodeDomain.https() = "https://$ipAddress:$port"
fun RemoteNodeDomain.https() = "https://$ipAddress:$port"