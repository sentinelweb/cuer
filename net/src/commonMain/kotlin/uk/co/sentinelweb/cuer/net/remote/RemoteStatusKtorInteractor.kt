package uk.co.sentinelweb.cuer.net.remote

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.client.RequestFailureException
import uk.co.sentinelweb.cuer.remote.server.AvailableMessageMapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.RequestMessage

internal class RemoteStatusKtorInteractor(
    private val availableService: RemoteStatusService,
    private val availableMessageMapper: AvailableMessageMapper,
    private val localRepository: LocalRepository,
    private val coroutines: CoroutineContextProvider,
) : RemoteStatusInteractor {

    override suspend fun available(
        messageType: AvailableMessage.MsgType,
        remote: RemoteNodeDomain,
    ): NetResult<Boolean> =
        withContext(coroutines.IO) {
            try {
                val availableMessage = AvailableMessage(
                    messageType,
                    availableMessageMapper.mapToMulticastMessage(localRepository.localNode)
                )
                val dto =
                    availableService.sendAvailable(remote, RequestMessage(availableMessage)) // todo ResponseMesage?
                NetResult.Data(true)
            } catch (e: RequestFailureException) {
                NetResult.HttpError(e)
            } catch (e: Exception) {
                NetResult.Error(e)
            }
        }

    override suspend fun sendTo(
        messageType: AvailableMessage.MsgType,
        remote: RemoteNodeDomain,
        target: RemoteNodeDomain
    ): NetResult<Boolean> =
        try {
            val availableMessage = AvailableMessage(
                messageType,
                availableMessageMapper.mapToMulticastMessage(remote)
            )
            val dto =
                availableService.sendAvailable(target, RequestMessage(availableMessage))
            NetResult.Data(true)
        } catch (e: RequestFailureException) {
            NetResult.HttpError(e)
        } catch (e: Exception) {
            NetResult.Error(e)
        }
}
