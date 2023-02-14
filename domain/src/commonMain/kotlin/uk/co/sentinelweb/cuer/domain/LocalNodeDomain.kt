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
    val authType: AuthConfig = AuthConfig.Open
) : NodeDomain() {

    @Serializable
    sealed class AuthConfig {
        object Open : AuthConfig()
        object Confirm : AuthConfig()

        @Serializable
        data class Username(val username: String, val password: String) : AuthConfig()
    }
}