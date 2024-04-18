package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.remote.server.AvailableMessageMapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.RequestMessage

internal class RemoteStatusKtorInteractor(
    private val availableService: RemoteAvailableService,
    private val availableMessageMapper: AvailableMessageMapper,
    private val localRepository: LocalRepository,
) : RemoteStatusInteractor {

    override suspend fun available(
        messageType: AvailableMessage.MsgType,
        remote: RemoteNodeDomain,
    ): NetResult<Boolean> {
        return try {
            val availableMessage = AvailableMessage(
                messageType,
                availableMessageMapper.mapToMulticastMessage(localRepository.localNode)
            )
            val dto = availableService.sendAvailable(remote, RequestMessage(availableMessage))
            NetResult.Data(true)
        } catch (e: Exception) {
            NetResult.Error(e)
        }
    }
}
