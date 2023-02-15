package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.remote.server.message.ConnectMessage

interface RemoteInteractor {

    @Throws(Exception::class)
    suspend fun connect(
        messageType: ConnectMessage.MsgType,
        remote: RemoteNodeDomain,
    ): NetResult<String>

}
