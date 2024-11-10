package uk.co.sentinelweb.cuer.remote.server.multicast

//import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier.Locator
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.ignoreJob
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.remote.server.*
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage.MsgType
import java.io.IOException
import java.net.*
import java.nio.charset.Charset

class JvmMultiCastSocket(
    private val config: MultiCastSocketContract.Config,
    private val log: LogWrapper,
    private val localRepository: LocalRepository,
    private val availableMessageMapper: AvailableMessageMapper,
    private val availableMessageHandler: RemoteServerContract.AvailableMessageHandler,
    private val coroutines: CoroutineContextProvider,
) : MultiCastSocketContract {

    private lateinit var broadcastAddress: InetSocketAddress
    private var isKeepGoing = true
    private var theSocket: MulticastSocket? = null

    init {
        log.tag(this)
    }

    override suspend fun runSocketListener(startListener: (() -> Unit)) = withContext(coroutines.IO) {
        try {
            printInterfaces()
            isKeepGoing = true
            broadcastAddress = InetSocketAddress(config.multicastIp, config.multicastPort)
            theSocket = MulticastSocket(config.multicastPort)
            // fixme play with this and maybe just get the iface by ip address?
            val networkInterface = findInterface(listOf("wlan0", "en0", "wlp", "eth0"))
            theSocket!!.networkInterface = networkInterface
            theSocket!!.joinGroup(broadcastAddress, networkInterface)
            val buffer = ByteArray(1 * 1024)
            val data = DatagramPacket(buffer, buffer.size)
            log.d("multi start: addr: $broadcastAddress config:${config.multicastIp}:${config.multicastPort}")
            startListener()
            while (isKeepGoing) {
                theSocket!!.receive(data) // blocks

                val msg = String(buffer, 0, data.length, Charset.defaultCharset())
                //log.d("multi Received: $msg")
                if (isKeepGoing) {
                    val msgDecoded = deserialiseMulti(msg)
                    availableMessageHandler.messageReceived(msgDecoded)
                }
            }
        } catch (e: IOException) {
            log.e(e.toString(), e)
        }
        log.d("exit")
    }

    private fun findInterface(names: List<String>): NetworkInterface? {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        for (netInterface in networkInterfaces) {
            if (netInterface.isUp && !netInterface.isLoopback) {
                //if (names.contains(netInterface.name)) {
                if (names.map { netInterface.name.contains(it) }.any()) {
                    return netInterface
                }
            }
        }
        return null
    }

    fun mapLocalNode() =
        availableMessageMapper.mapToMulticastMessage(localRepository.localNode)

    override suspend fun send(msgType: MsgType) {
        if (theSocket != null && !theSocket!!.isClosed) {
            try {
                val joinMsg = AvailableMessage(msgType, mapLocalNode())
                sendDatagram(joinMsg)
                log.d("sendMulticast($msgType) .. done")
            } catch (e: Exception) {
                log.e("sendMulticast($msgType) err", e)
            }
        }
    }

    override fun close(): Unit = coroutines.ioScope.launch {
        isKeepGoing = false
        theSocket?.also { socket ->
            if (!socket.isClosed) {
                // TODO note we might need to send a local exit message to stop blocking ...
                try {
                    val closeMsg = AvailableMessage(MsgType.Close, mapLocalNode())
                    sendDatagram(closeMsg)
                    //log.d("multi closing: send lastcall")
                    // todo check if i need this
                    // fixme does this return the right IP for multicast?
                    val local = InetAddress.getLocalHost()
                    val data1 = DatagramPacket("".toByteArray(), 0, local, config.multicastPort)
                    socket.send(data1)
                    socket.close()
                    log.d("multi closed")
                } catch (e: Exception) {
                    log.e("multi close ex: ", e)
                }
            }
        } ?: throw IllegalStateException("Socket not available")
    }.ignoreJob()

    private fun sendDatagram(msg: AvailableMessage) {
        val serialise = msg.serialise()
        val data = DatagramPacket(serialise.toByteArray(), serialise.length, broadcastAddress)
        theSocket?.send(data) ?: throw IllegalStateException("Socket not available")
    }

    fun printInterfaces() {
        log.d("--------NETWORK INTERFACES -------------")
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        for (netInterface in networkInterfaces) {
            if (netInterface.isUp && !netInterface.isLoopback) {
                log.d(netInterface.run { "if:$name addr: $interfaceAddresses isMulti:${supportsMulticast()} isUp:$isUp isLoopback:$isLoopback isKeepGoing:$isKeepGoing isVirtual:$isVirtual" })
            }
        }
        log.d("----------------------------------------")
    }
}