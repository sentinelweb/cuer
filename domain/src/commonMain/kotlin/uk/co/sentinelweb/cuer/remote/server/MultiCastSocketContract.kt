package uk.co.sentinelweb.cuer.remote.server

import kotlinx.serialization.Serializable

interface MultiCastSocketContract {

    var recieveListener: ((MulticastMessage) -> Unit)?
    var startListener: (() -> Unit)?
    fun runSocketListener()
    fun sendJoin()
    fun close()

    @Serializable
    sealed class MulticastMessage {

        @Serializable
        data class Join(val join: String) : MulticastMessage()

        @Serializable
        data class Ping(val ping: String) : MulticastMessage()

        @Serializable
        data class Close(val close: String) : MulticastMessage()

    }

    data class Config(
        val ip: String = MULTICAST_IP_DEF,
        val port: Int = MULTICAST_PORT_DEF,
        val webPort: Int = SERVERPORT_DEF,
    )

    companion object {
        const val SERVERPORT_DEF = 4444
        const val MULTICAST_PORT_DEF = 4445
        const val MULTICAST_IP_DEF = "224.0.0.1"

    }
}