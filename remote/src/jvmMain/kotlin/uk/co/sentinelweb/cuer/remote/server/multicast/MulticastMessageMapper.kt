package uk.co.sentinelweb.cuer.remote.server.multicast

import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract.MulticastMessage

class MulticastMessageMapper(private val config: BuildConfigDomain) {

    fun mapToMulticastMessage(localNode: LocalNodeDomain): MulticastMessage.DeviceInfo {
        return MulticastMessage.DeviceInfo(
            id = localNode.id,
            hostname = localNode.hostname,
            deviceType = localNode.deviceType,
            version = config.version,
            ipAddress = localNode.ipAddress,
            port = localNode.port,
            device = localNode.device,
            authType = mapAuthType(localNode.authType),
            versionCode = config.versionCode,
        )
    }

    private fun mapAuthType(authType: LocalNodeDomain.AuthConfig): MulticastMessage.AuthMethod = when (authType) {
        is LocalNodeDomain.AuthConfig.Open -> MulticastMessage.AuthMethod.Open
        is LocalNodeDomain.AuthConfig.Username -> MulticastMessage.AuthMethod.Username
        is LocalNodeDomain.AuthConfig.Confirm -> MulticastMessage.AuthMethod.Confirm
    }

    fun mapFromMulticastMessage(msg: MulticastMessage.DeviceInfo): RemoteNodeDomain {
        return RemoteNodeDomain(
            id = msg.id,
            ipAddress = msg.ipAddress,
            port = msg.port,
            hostname = msg.hostname,
            device = msg.device,
            deviceType = msg.deviceType,
            authType = mapAuthType(msg.authType),
            version = msg.version,
            versionCode = msg.versionCode,
        )
    }

    private fun mapAuthType(authType: MulticastMessage.AuthMethod): RemoteNodeDomain.AuthType = when (authType) {
        MulticastMessage.AuthMethod.Open -> RemoteNodeDomain.AuthType.Open
        MulticastMessage.AuthMethod.Username -> RemoteNodeDomain.AuthType.Username()
        MulticastMessage.AuthMethod.Confirm -> RemoteNodeDomain.AuthType.Token()
    }
}
