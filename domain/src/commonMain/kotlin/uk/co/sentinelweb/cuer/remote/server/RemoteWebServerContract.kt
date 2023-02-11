package uk.co.sentinelweb.cuer.remote.server

interface RemoteWebServerContract {
    val port: Int
    fun fullAddress(ip: String): String
    val isRunning: Boolean
    fun start()
    fun stop()
}