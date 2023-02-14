package uk.co.sentinelweb.cuer.domain

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract

@Serializable
data class RemoteNodeDomain(
    val id: OrchestratorContract.Identifier<GUID>?,
    val ipAddress: String,
    val port: Int,
    val hostname: String? = null,
    val device: String? = null,
    val deviceType: DeviceType? = null,
    val authType: AuthType = AuthType.Open,
    val lastRead: Instant? = null,
    val lastWrite: Instant? = null,
    val dateAdded: Instant? = null,
    val version: String,
    val versionCode: Int,
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