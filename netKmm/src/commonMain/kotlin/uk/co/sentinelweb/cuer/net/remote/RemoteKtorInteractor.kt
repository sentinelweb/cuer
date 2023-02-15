package uk.co.sentinelweb.cuer.net.remote

import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.remote.server.ConnectMessageMapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.message.ConnectMessage
import uk.co.sentinelweb.cuer.remote.server.message.RequestMessage

internal class RemoteKtorInteractor(
    private val connectService: RemoteConnectService,
    private val connectMessageMapper: ConnectMessageMapper,
    private val localRepository: LocalRepository,
) : RemoteInteractor {

    override suspend fun connect(
        messageType: ConnectMessage.MsgType,
        remote: RemoteNodeDomain,
    ): NetResult<String> {
        return try {
            val connectMessage = ConnectMessage(messageType, connectMessageMapper.mapToMulticastMessage(localRepository.getLocalNode(), true))
            val dto = connectService.sendConnect(remote, RequestMessage(connectMessage))
            NetResult.Data(dto.toString())
        } catch (e: Exception) {
            NetResult.Error(e)
        }
    }
}
