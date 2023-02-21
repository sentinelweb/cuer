package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage

class AvailableMessageMapper(
    private val config: BuildConfigDomain,
    private val connectivityWrapper: ConnectivityWrapper,
) {

    fun mapToMulticastMessage(localNode: LocalNodeDomain, refreshIp: Boolean): AvailableMessage.DeviceInfo {
        return AvailableMessage.DeviceInfo(
            id = localNode.id,
            hostname = localNode.hostname,
            deviceType = localNode.deviceType,
            version = config.version,
            ipAddress = if (refreshIp) connectivityWrapper.getWIFIIP() ?: localNode.ipAddress else localNode.ipAddress,
            port = localNode.port,
            device = localNode.device,
            authType = mapAuthType(localNode.authConfig),
            versionCode = config.versionCode,
        )
    }

    private fun mapAuthType(authType: LocalNodeDomain.AuthConfig): AvailableMessage.AuthMethod = when (authType) {
        is LocalNodeDomain.AuthConfig.Open -> AvailableMessage.AuthMethod.Open
        is LocalNodeDomain.AuthConfig.Username -> AvailableMessage.AuthMethod.Username
        is LocalNodeDomain.AuthConfig.Confirm -> AvailableMessage.AuthMethod.Confirm
    }

    fun mapFromMulticastMessage(msg: AvailableMessage.DeviceInfo): RemoteNodeDomain {
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

    private fun mapAuthType(authType: AvailableMessage.AuthMethod): RemoteNodeDomain.AuthType = when (authType) {
        AvailableMessage.AuthMethod.Open -> RemoteNodeDomain.AuthType.Open
        AvailableMessage.AuthMethod.Username -> RemoteNodeDomain.AuthType.Username()
        AvailableMessage.AuthMethod.Confirm -> RemoteNodeDomain.AuthType.Token()
    }
}
