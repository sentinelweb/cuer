package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.WEB_SERVER_PORT_DEF
import uk.co.sentinelweb.cuer.remote.server.message.ConnectMessage

interface MultiCastSocketContract {

    var startListener: (() -> Unit)?
    var connectMessageListener: ((ConnectMessage) -> Unit)?

    fun runSocketListener()
    fun send(msgType: ConnectMessage.MsgType)
    fun close()


    data class Config(
        val ip: String = MULTICAST_IP_DEF,
        val port: Int = MULTICAST_PORT_DEF,
        val webPort: Int = WEB_SERVER_PORT_DEF,
    )

    companion object {
        const val MULTICAST_PORT_DEF = 9091
        const val MULTICAST_IP_DEF = "224.0.0.1"
    }
}