package uk.co.sentinelweb.cuer.remote.server

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.WEB_SERVER_PORT_DEF

interface MultiCastSocketContract {

    var startListener: (() -> Unit)?
    var recieveListener: ((MulticastMessage.MsgType, RemoteNodeDomain) -> Unit)?

    fun runSocketListener()
    fun send(msgType: MulticastMessage.MsgType)
    fun close()


    @Serializable
    data class MulticastMessage(val type: MsgType, val node: DeviceInfo) {
        enum class MsgType { Join, JoinReply, Ping, PingReply, Close }

        @Serializable
        data class DeviceInfo(
            val id: OrchestratorContract.Identifier<GUID>?,
            val ipAddress: String,
            val port: Int,
            val hostname: String?,
            val device: String?,
            val deviceType: NodeDomain.DeviceType?,
            val authType: AuthMethod,
            val version: String,
            val versionCode: Int,
        )

        enum class AuthMethod { Open, Username, Confirm }
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