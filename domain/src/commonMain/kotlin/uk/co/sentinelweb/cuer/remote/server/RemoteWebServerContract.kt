package uk.co.sentinelweb.cuer.remote.server

interface RemoteWebServerContract {
    val port: Int
    val isRunning: Boolean

    fun start(onStarted: () -> Unit)

    fun stop()

    companion object {
        const val WEB_SERVER_PORT_DEF = 9090

        object CONNECT_API {
            val PATH = "/connect"
        }
    }
}