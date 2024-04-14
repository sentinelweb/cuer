package uk.co.sentinelweb.cuer.remote.server.message

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.NodeDomain

@Serializable
data class AvailableMessage(val type: MsgType, val node: DeviceInfo) : Message {

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
