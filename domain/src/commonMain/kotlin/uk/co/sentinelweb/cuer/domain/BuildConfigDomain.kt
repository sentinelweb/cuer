package uk.co.sentinelweb.cuer.domain

data class BuildConfigDomain(
    val isDebug: Boolean,
    val cuerRemoteEnabled: Boolean,
    val versionCode: Int,
    val version: String,
)
