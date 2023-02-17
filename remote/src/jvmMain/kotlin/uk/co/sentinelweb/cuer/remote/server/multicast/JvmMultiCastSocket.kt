package uk.co.sentinelweb.cuer.remote.server.multicast

import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.remote.server.ConnectMessageMapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract
import uk.co.sentinelweb.cuer.remote.server.message.ConnectMessage
import uk.co.sentinelweb.cuer.remote.server.message.ConnectMessage.MsgType
import uk.co.sentinelweb.cuer.remote.server.message.deserialiseMulti
import uk.co.sentinelweb.cuer.remote.server.message.serialise
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.charset.Charset

// fixme copied from jvm
class JvmMultiCastSocket(
    private val config: MultiCastSocketContract.Config,
    private val log: LogWrapper,
    private val localRepository: LocalRepository,
    private val connectMessageMapper: ConnectMessageMapper,
) : MultiCastSocketContract {

    override var connectMessageListener: ((ConnectMessage) -> Unit)? = null
    override var startListener: (() -> Unit)? = null

    private lateinit var broadcastAddress: InetAddress
    private var isKeepGoing = true
    private var theSocket: MulticastSocket? = null

    init {
        log.tag(this)
    }

    override fun runSocketListener() {
        try {
            isKeepGoing = true
            broadcastAddress = InetAddress.getByName(config.ip)
            theSocket = MulticastSocket(config.port)
            theSocket!!.joinGroup(broadcastAddress)
            val buffer = ByteArray(1 * 1024)
            val data1 = DatagramPacket(buffer, buffer.size)
            log.d("multi start: addr: $broadcastAddress")
            startListener?.invoke()
            while (isKeepGoing) {
                theSocket!!.receive(data1) // blocks
                val msg = String(buffer, 0, data1.length, Charset.defaultCharset())
                //log.d("multi Received: $msg")
                if (isKeepGoing) {
                    val msgDecoded = deserialiseMulti(msg)
                    connectMessageListener?.invoke(msgDecoded)
                }
            }
        } catch (e: IOException) {
            log.e(e.toString(), e)
        }
        log.d("exit")
    }

    fun mapLocalNode() =
        connectMessageMapper.mapToMulticastMessage(localRepository.getLocalNode(), true)

    override fun send(msgType: MsgType) {
        if (theSocket != null && !theSocket!!.isClosed) {
            try {
                val joinMsg = ConnectMessage(msgType, mapLocalNode())
                sendDatagram(joinMsg)
                log.d("sendMulticast($msgType) .. done")
            } catch (e: Exception) {
                log.e("sendMulticast($msgType) err", e)
            }
        }
    }

    override fun close() {
        isKeepGoing = false
        if (theSocket != null && !theSocket!!.isClosed) {
            // TODO note we might need to send a local exit message to stop blocking ...
            try {
                val closeMsg = ConnectMessage(MsgType.Close, mapLocalNode())
                sendDatagram(closeMsg)
                //log.d("multi closing: send lastcall")
                val local = InetAddress.getByName("localhost")
                val data1 = DatagramPacket("".toByteArray(), 0, local, config.port)
                theSocket!!.send(data1)
                theSocket!!.close()
                log.d("multi closed")
            } catch (e: Exception) {
                log.e("multi close ex: ", e)
            }
        }
    }

    private fun sendDatagram(msg: ConnectMessage) {
        val serialise = msg.serialise()
        val data = DatagramPacket(serialise.toByteArray(), serialise.length, broadcastAddress, config.port)
        theSocket!!.send(data)
    }
}