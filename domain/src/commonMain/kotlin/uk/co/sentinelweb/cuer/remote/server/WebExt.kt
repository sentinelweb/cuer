package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain


fun Pair<String, Int>.ipport() = "$first:$second"
fun LocalNodeDomain.ipport() = "$ipAddress:$port"
fun RemoteNodeDomain.ipport() = "$ipAddress:$port"
fun Locator.ipport() = "$address:$port"

fun RemoteNodeDomain.locator() = Locator(ipAddress, port)
fun LocalNodeDomain.locator() = Locator(ipAddress, port)

fun Pair<String, Int>.http() = "http://$first:$second"
fun LocalNodeDomain.http() = "http://$ipAddress:$port"
fun RemoteNodeDomain.http() = "http://$ipAddress:$port"
fun Locator.http() = "http://$address:$port"

fun Pair<String, Int>.https() = "https://$first:$second"
fun LocalNodeDomain.https() = "https://$ipAddress:$port"
fun RemoteNodeDomain.https() = "https://$ipAddress:$port"
fun Locator.https() = "https://$address:$port"
