package uk.co.sentinelweb.cuer.domain.system

import kotlinx.serialization.Serializable

@Serializable
data class ErrorDomain constructor(
    val level: Level,
    val type: Type,
    val code: Int,
    val message: String,
    val exception: String? = null
) {
    enum class Type {
        HTTP, DATABASE, NETWORK, APP
    }

    enum class Level {
        INFO, WARNING, ERROR
    }
}