package uk.co.sentinelweb.cuer.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract

@Serializable
data class NodeDomain(
    val id: OrchestratorContract.Identifier<GUID>?,
    val ipAddress: String,
    val port: Int,
    val hostname: String? = null,
    val device: String? = null,
    val deviceType: DeviceType? = null,
    val authType: List<AuthConfig> = listOf(),
    val lastRead: Instant? = null,
    val lastWrite: Instant? = null,
    val dateAdded: Instant? = null,
) : Domain {

    @Serializable
    sealed class AuthConfig {
        object None : AuthConfig()
        data class Username(val username: String, val password: String, val lastLoginDate: Instant? = null) : AuthConfig()
        data class Token(val token: String, val tokenDate: Instant? = null) : AuthConfig()
    }

    enum class DeviceType {
        ANDROID,
        IOS,
        WEB,
        OTHER
    }
}