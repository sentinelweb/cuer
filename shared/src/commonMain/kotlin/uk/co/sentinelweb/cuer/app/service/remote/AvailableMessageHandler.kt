package uk.co.sentinelweb.cuer.app.service.remote

//import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
//import uk.co.sentinelweb.cuer.app.orchestrator.toLocalNetworkIdentifier
import uk.co.sentinelweb.cuer.app.util.wrapper.VibrateWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.net.remote.RemoteStatusInteractor
import uk.co.sentinelweb.cuer.remote.server.AvailableMessageMapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage.MsgType.*

class AvailableMessageHandler(
    private val remoteRepo: RemotesRepository,
    private val availableMessageMapper: AvailableMessageMapper,
    private val remoteStatusInteractor: RemoteStatusInteractor,
    private val localRepo: LocalRepository,
    private val wifiStateProvider: WifiStateProvider,
    private val vibrateWrapper: VibrateWrapper,
    private val log: LogWrapper,
) : RemoteServerContract.AvailableMessageHandler {

    init {
        log.tag(this)
    }

    override suspend fun messageReceived(msg: AvailableMessage) {
        val remote = mapRemoteNode(msg).copy(isAvailable = msg.type != Close)
        log.d("receive connect: ${msg.type} remote: $remote")
        when (msg.type) {
            Join -> remoteRepo.addUpdateNode(remote)
            Close -> remoteRepo.addUpdateNode(remote)
            Ping -> remoteRepo.addUpdateNode(remote)
            PingReply -> remoteRepo.addUpdateNode(remote)
            JoinReply -> remoteRepo.addUpdateNode(remote)
        }

        when (msg.type) {
            Join -> vibrateWrapper.vibrate()
            Ping -> vibrateWrapper.vibrate()
            else -> Unit
        }

        if (localRepo.localNode.id != remote.id) {
            when (msg.type) {
                Join -> remoteStatusInteractor.available(JoinReply, remote)
                Ping -> remoteStatusInteractor.available(PingReply, remote)
                else -> Unit
            }
        }
    }

    private fun mapRemoteNode(msgDecoded: AvailableMessage) =
        availableMessageMapper.mapFromMulticastMessage(msgDecoded.node, wifiStateProvider.wifiState)
}
