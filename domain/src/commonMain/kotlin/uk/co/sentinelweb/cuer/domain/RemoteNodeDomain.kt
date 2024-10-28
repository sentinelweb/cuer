package uk.co.sentinelweb.cuer.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier

@Serializable
data class RemoteNodeDomain(
    val id: Identifier<GUID>?,
    val ipAddress: String,
    val port: Int,
    val hostname: String? = null,
    val ssid: String? = null,
    val isAvailable: Boolean = false,
    val device: String? = null,
    val deviceType: DeviceType? = null,
    val authType: AuthType = AuthType.Open,
    val lastRead: Instant? = null,
    val lastWrite: Instant? = null,
    val dateAdded: Instant? = null,
    val version: String? = null,
    val versionCode: Int? = null,
) : NodeDomain() {

    @Serializable
    sealed class AuthType {
        @Serializable
        object Open : AuthType()

        @Serializable
        data class Username(
            val username: String? = null,
            val password: String? = null,
            val lastLoginDate: Instant? = null,
            val token: String? = null,
            val tokenDate: Instant? = null
        ) : AuthType()

        @Serializable
        data class Token(
            val token: String? = null,
            val tokenDate: Instant? = null
        ) : AuthType()
    }
}
