package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.WEB_SERVER_PORT_DEF
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage

interface MultiCastSocketContract {

    var startListener: (() -> Unit)?

    suspend fun runSocketListener()
    suspend fun send(msgType: AvailableMessage.MsgType)
    fun close()

    data class Config(
        val ip: String = MULTICAST_IP_DEF,
        val multiPort: Int = MULTICAST_PORT_DEF,
        val webPort: Int = WEB_SERVER_PORT_DEF,
    )

    companion object {
        const val MULTICAST_PORT_DEF = 9091
        const val MULTICAST_PORT_DEBUG_DEF = 9092

        //const val MULTICAST_IP_DEF = "224.0.0.1"
        const val MULTICAST_IP_DEF = "239.1.2.3"
    }
}