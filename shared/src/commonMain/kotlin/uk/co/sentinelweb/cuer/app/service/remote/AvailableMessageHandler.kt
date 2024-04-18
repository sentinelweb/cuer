package uk.co.sentinelweb.cuer.app.service.remote

import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.net.remote.RemoteStatusInteractor
import uk.co.sentinelweb.cuer.remote.server.AvailableMessageMapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage

class AvailableMessageHandler(
    private val remoteRepo: RemotesRepository,
    private val availableMessageMapper: AvailableMessageMapper,
    private val remoteStatusInteractor: RemoteStatusInteractor,
    private val localRepo: LocalRepository,
    private val log: LogWrapper,
) : RemoteServerContract.AvailableMessageHandler {

    init {
        log.tag(this)
    }

    override suspend fun messageReceived(msg: AvailableMessage) {
        val remote = mapRemoteNode(msg).copy(isAvailable = msg.type != AvailableMessage.MsgType.Close)
        log.d("receive connect: ${msg.type} remote: $remote")
        when (msg.type) {
            AvailableMessage.MsgType.Join -> remoteRepo.addUpdateNode(remote)
            AvailableMessage.MsgType.Close -> remoteRepo.addUpdateNode(remote)
            AvailableMessage.MsgType.Ping -> remoteRepo.addUpdateNode(remote)
            AvailableMessage.MsgType.PingReply -> remoteRepo.addUpdateNode(remote)
            AvailableMessage.MsgType.JoinReply -> remoteRepo.addUpdateNode(remote)
        }

        if (localRepo.localNode.id != remote.id) {
            when (msg.type) {
                AvailableMessage.MsgType.Join -> remoteStatusInteractor.available(
                    AvailableMessage.MsgType.JoinReply,
                    remote
                )

                AvailableMessage.MsgType.Ping -> remoteStatusInteractor.available(
                    AvailableMessage.MsgType.PingReply,
                    remote
                )

                else -> Unit
            }
        }
    }

    private fun mapRemoteNode(msgDecoded: AvailableMessage) =
        availableMessageMapper.mapFromMulticastMessage(msgDecoded.node)
}
