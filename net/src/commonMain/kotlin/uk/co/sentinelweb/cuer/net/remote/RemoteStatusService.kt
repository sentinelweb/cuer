package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.client.ServiceExecutor
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.AVAILABLE_API
import uk.co.sentinelweb.cuer.remote.server.ipport
import uk.co.sentinelweb.cuer.remote.server.message.RequestMessage

internal class RemoteStatusService(
    private val executor: ServiceExecutor
) {

    internal suspend fun sendAvailable(
        node: RemoteNodeDomain,
        msg: RequestMessage// todo ResponseMessage?
    ): Unit = executor.post(
        path = node.ipport() + AVAILABLE_API.PATH,
        body = msg,
    )
}
