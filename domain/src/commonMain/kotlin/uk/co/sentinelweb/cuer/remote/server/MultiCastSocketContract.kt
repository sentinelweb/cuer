package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.WEB_SERVER_PORT_DEF
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage

interface MultiCastSocketContract {

    suspend fun runSocketListener(startListener: (() -> Unit))

    suspend fun send(msgType: AvailableMessage.MsgType)

    fun close()

    data class Config(
        val multicastIp: String = MULTICAST_IP_DEF,
        val multicastPort: Int = MULTICAST_PORT_DEF,
        val webServerPort: Int = WEB_SERVER_PORT_DEF,
    )

    companion object {
        const val MULTICAST_PORT_DEF = 9091
        const val MULTICAST_PORT_DEBUG_DEF = 9092

        //const val MULTICAST_IP_DEF = "224.0.0.1"
        // Arbitrary multicast IP (224.0.0.0/4) - needs a config
        const val MULTICAST_IP_DEF = "239.25.123.46"
    }
}