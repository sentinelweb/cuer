package uk.co.sentinelweb.cuer.domain

data class BuildConfigDomain(
    val isDebug: Boolean,
    val versionCode: Int,
    val version: String,
)
