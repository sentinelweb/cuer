package uk.co.sentinelweb.cuer.app.service.remote

interface MultiCastSocketContract {

    var recieveListener: ((String) -> Unit)?
    var startListener: (() -> Unit)?
    fun runSocketListener()
    fun sendBroadcast()
    fun close()

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