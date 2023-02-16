package uk.co.sentinelweb.cuer.domain

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract

@Serializable
data class LocalNodeDomain(
    val id: OrchestratorContract.Identifier<GUID>?,
    val ipAddress: String,
    val port: Int,
    val hostname: String? = null,
    val device: String? = null,
    val deviceType: DeviceType? = null,
    val authConfig: AuthConfig = AuthConfig.Open,
    val version: String? = null,
    val versionCode: Int? = null,
    val wifiAutoNotify: Boolean = false,
    val wifiAutoStart: Boolean = false,
    val wifiAutoConnectSSIDs: List<String> = listOf(),
) : NodeDomain() {

    @Serializable
    sealed class AuthConfig {
        @Serializable
        object Open : AuthConfig()

        @Serializable
        object Confirm : AuthConfig()

        @Serializable
        data class Username(val username: String, val password: String) : AuthConfig()
    }
}
