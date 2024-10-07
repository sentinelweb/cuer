package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage

interface RemoteStatusInteractor {

    @Throws(Exception::class)
    suspend fun available(
        messageType: AvailableMessage.MsgType,
        remote: RemoteNodeDomain,
    ): NetResult<Boolean>

    @Throws(Exception::class)
    suspend fun sendTo(
        messageType: AvailableMessage.MsgType,
        remote: RemoteNodeDomain,
        target: RemoteNodeDomain,
    ): NetResult<Boolean>

}
