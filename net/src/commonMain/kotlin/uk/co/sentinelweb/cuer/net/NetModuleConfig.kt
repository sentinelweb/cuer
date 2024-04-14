package uk.co.sentinelweb.cuer.net

data class NetModuleConfig constructor(
    val debug: Boolean = false,
    val timeoutMs: Long = 5000
)