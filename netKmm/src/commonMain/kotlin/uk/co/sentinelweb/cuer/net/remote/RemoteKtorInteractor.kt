package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.remote.server.AvailableMessageMapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.RequestMessage

internal class RemoteKtorInteractor(
    private val availableService: RemoteAvailableService,
    private val availableMessageMapper: AvailableMessageMapper,
    private val localRepository: LocalRepository,
) : RemoteInteractor {

    override suspend fun available(
        messageType: AvailableMessage.MsgType,
        remote: RemoteNodeDomain,
    ): NetResult<Boolean> {
        return try {
            val availableMessage = AvailableMessage(messageType, availableMessageMapper.mapToMulticastMessage(localRepository.getLocalNode(), true))
            val dto = availableService.sendAvailable(remote, RequestMessage(availableMessage))
            NetResult.Data(true)
        } catch (e: Exception) {
            NetResult.Error(e)
        }
    }
}
