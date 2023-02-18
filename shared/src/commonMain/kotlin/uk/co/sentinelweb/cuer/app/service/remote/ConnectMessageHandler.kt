package uk.co.sentinelweb.cuer.app.service.remote

import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.net.remote.RemoteInteractor
import uk.co.sentinelweb.cuer.remote.server.ConnectMessageMapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.message.ConnectMessage

class ConnectMessageHandler(
    private val remoteRepo: RemotesRepository,
    private val connectMessageMapper: ConnectMessageMapper,
    private val remoteInteractor: RemoteInteractor,
    private val localRepo: LocalRepository,
    private val log: LogWrapper,
) : RemoteServerContract.ConnectMessageHandler {

    init {
        log.tag(this)
    }

    override suspend fun messageReceived(msg: ConnectMessage) {
        val remote = mapRemoteNode(msg).copy(isConnected = msg.type != ConnectMessage.MsgType.Close)
        log.d("receive connect: ${msg.type} remote: $remote")
        when (msg.type) {
            ConnectMessage.MsgType.Join -> remoteRepo.addUpdateNode(remote)
            ConnectMessage.MsgType.Close -> remoteRepo.addUpdateNode(remote)
            ConnectMessage.MsgType.Ping -> remoteRepo.addUpdateNode(remote)
            ConnectMessage.MsgType.PingReply -> remoteRepo.addUpdateNode(remote)
            ConnectMessage.MsgType.JoinReply -> remoteRepo.addUpdateNode(remote)
        }

        if (localRepo.getLocalNode().id != remote.id) {
            when (msg.type) {
                ConnectMessage.MsgType.Join -> remoteInteractor.connect(ConnectMessage.MsgType.JoinReply, remote)
                ConnectMessage.MsgType.Ping -> remoteInteractor.connect(ConnectMessage.MsgType.PingReply, remote)

                else -> Unit
            }
        }
    }

    private fun mapRemoteNode(msgDecoded: ConnectMessage) =
        connectMessageMapper.mapFromMulticastMessage(msgDecoded.node)
}
