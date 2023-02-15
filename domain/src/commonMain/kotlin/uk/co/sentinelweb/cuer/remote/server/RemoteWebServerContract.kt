package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.remote.server.message.ConnectMessage

interface RemoteWebServerContract {
    val port: Int
    val isRunning: Boolean
    var connectMessageListener: ((ConnectMessage) -> Unit)?

    fun start(onStarted: () -> Unit)

    fun stop()

    companion object {
        const val WEB_SERVER_PORT_DEF = 9090

        object CONNECT_API {
            val PATH = "/connect"
        }
    }
}