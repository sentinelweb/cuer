package uk.co.sentinelweb.cuer.remote.server

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.WEB_SERVER_PORT_DEF

interface MultiCastSocketContract {

    var startListener: (() -> Unit)?
    var recieveListener: ((MulticastMessage) -> Unit)?

    fun runSocketListener()
    fun send(msgType: MulticastMessage.MsgType)
    fun close()

    @Serializable
    data class MulticastMessage(val type: MsgType, val node: NodeDomain) {
        enum class MsgType { Join, JoinReply, Ping, PingReply, Close }

    }

    data class Config(
        val ip: String = MULTICAST_IP_DEF,
        val port: Int = MULTICAST_PORT_DEF,
        val webPort: Int = WEB_SERVER_PORT_DEF,
    )

    companion object {
        const val MULTICAST_PORT_DEF = 9091
        const val MULTICAST_IP_DEF = "224.0.0.1"
    }
}