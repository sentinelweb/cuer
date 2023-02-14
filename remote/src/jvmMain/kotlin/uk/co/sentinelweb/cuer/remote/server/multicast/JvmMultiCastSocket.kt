package uk.co.sentinelweb.cuer.remote.server.multicast

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract.MulticastMessage
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract.MulticastMessage.MsgType
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.charset.Charset

// fixme copied from jvm
class JvmMultiCastSocket(
    private val config: MultiCastSocketContract.Config,
    private val log: LogWrapper,
    private val connectivityUtil: ConnectivityWrapper,
    private val buildConfigDomain: BuildConfigDomain,
    private val localRepository: LocalRepository,
    private val multicastMessageMapper: MulticastMessageMapper,
) : MultiCastSocketContract {

    override var recieveListener: ((MsgType, RemoteNodeDomain) -> Unit)? = null
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
                val msgDecoded = deserialiseMulti(msg)
                if (recieveListener != null) {
                    recieveListener?.invoke(msgDecoded.type, mapRemoteNode(msgDecoded))
                }
                // todo reply via web server
                if (msgDecoded.type == MsgType.Ping) {
                    send(MsgType.PingReply)
                }
                if (msgDecoded.type == MsgType.Join) {
                    send(MsgType.JoinReply)
                }
            }
        } catch (e: IOException) {
            log.e(e.toString(), e)
        }
        log.d("exit")
    }

    private fun mapRemoteNode(msgDecoded: MulticastMessage) = multicastMessageMapper.mapFromMulticastMessage(msgDecoded.node)

    fun mapLocalNode() = multicastMessageMapper.mapToMulticastMessage(localRepository.getLocalNode())

    override fun send(msgType: MsgType) {
        if (theSocket != null && !theSocket!!.isClosed) {
            try {
                val joinMsg = MulticastMessage(msgType, mapLocalNode())
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
                val closeMsg = MulticastMessage(MsgType.Close, mapLocalNode())
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

    private fun sendDatagram(msg: MulticastMessage) {
        val serialise = msg.serialise()
        val data = DatagramPacket(serialise.toByteArray(), serialise.length, broadcastAddress, config.port)
        theSocket!!.send(data)
    }


    // region JSON serializer
    ///////////////////////////////////////////////////////////////////////////
    fun MulticastMessage.serialise() = wifiJsonSerializer.encodeToString(MulticastMessage.serializer(), this)
    fun deserialiseMulti(json: String) = wifiJsonSerializer.decodeFromString(MulticastMessage.serializer(), json)

    private val muiltcastSerializersModule = SerializersModule {
        mapOf(
            MulticastMessage::class to MulticastMessage.serializer(),
        )
    }.plus(SerializersModule {
        contextual(Instant::class, InstantIso8601Serializer)
    }).plus(SerializersModule {
        contextual(LocalDateTime::class, LocalDateTimeIso8601Serializer)
    })

    val wifiJsonSerializer = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        serializersModule = muiltcastSerializersModule
    }
    //endregion
}