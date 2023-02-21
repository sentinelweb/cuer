package uk.co.sentinelweb.cuer.remote.server.multicast

import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.remote.server.AvailableMessageMapper
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage.MsgType
import uk.co.sentinelweb.cuer.remote.server.message.deserialiseMulti
import uk.co.sentinelweb.cuer.remote.server.message.serialise
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.charset.Charset

class JvmMultiCastSocket(
    private val config: MultiCastSocketContract.Config,
    private val log: LogWrapper,
    private val localRepository: LocalRepository,
    private val availableMessageMapper: AvailableMessageMapper,
    private val availableMessageHandler: RemoteServerContract.AvailableMessageHandler,
) : MultiCastSocketContract {

    override var startListener: (() -> Unit)? = null

    private lateinit var broadcastAddress: InetAddress
    private var isKeepGoing = true
    private var theSocket: MulticastSocket? = null

    init {
        log.tag(this)
    }

    override suspend fun runSocketListener() {
        try {
            isKeepGoing = true
            broadcastAddress = InetAddress.getByName(config.ip)
            theSocket = MulticastSocket(config.multiPort)
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
                    availableMessageHandler.messageReceived(msgDecoded)
                }
            }
        } catch (e: IOException) {
            log.e(e.toString(), e)
        }
        log.d("exit")
    }

    fun mapLocalNode() =
        availableMessageMapper.mapToMulticastMessage(localRepository.getLocalNode(), true)

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

    override fun close() {
        isKeepGoing = false
        if (theSocket != null && !theSocket!!.isClosed) {
            // TODO note we might need to send a local exit message to stop blocking ...
            try {
                val closeMsg = AvailableMessage(MsgType.Close, mapLocalNode())
                sendDatagram(closeMsg)
                //log.d("multi closing: send lastcall")
                // todo check if i need this
                val local = InetAddress.getByName("localhost")
                val data1 = DatagramPacket("".toByteArray(), 0, local, config.multiPort)
                theSocket!!.send(data1)
                theSocket!!.close()
                log.d("multi closed")
            } catch (e: Exception) {
                log.e("multi close ex: ", e)
            }
        }
    }

    private fun sendDatagram(msg: AvailableMessage) {
        val serialise = msg.serialise()
        val data = DatagramPacket(serialise.toByteArray(), serialise.length, broadcastAddress, config.multiPort)
        theSocket!!.send(data)
    }
}