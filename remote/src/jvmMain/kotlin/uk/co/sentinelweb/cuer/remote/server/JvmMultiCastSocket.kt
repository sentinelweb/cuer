package uk.co.sentinelweb.cuer.remote.server

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract.MulticastMessage
import uk.co.sentinelweb.cuer.remote.server.MultiCastSocketContract.MulticastMessage.*
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.charset.Charset

// fixme copied from jvm
class JvmMultiCastSocket(
    private val config: MultiCastSocketContract.Config,
    private val log: LogWrapper,
    private val connectivityUtil: ConnectivityWrapper
) : MultiCastSocketContract {

    override var recieveListener: ((MulticastMessage) -> Unit)? = null
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
            sendJoin()
            val buffer = ByteArray(10 * 1024)
            val data1 = DatagramPacket(buffer, buffer.size)
            log.d("multi start: addr:$broadcastAddress")
            startListener?.invoke()
            while (isKeepGoing) {
                theSocket!!.receive(data1) // blocks
                val msg = String(buffer, 0, data1.length, Charset.defaultCharset())
                log.d("multi Received: $msg")
                if (recieveListener != null) {
                    recieveListener?.invoke(deserialiseMulti(msg))
                }
            }
        } catch (e: IOException) {
            log.e(e.toString(), e)
        }
        log.d("exit")
    }

    override fun sendJoin() {
        if (theSocket != null && !theSocket!!.isClosed) {
            try {
                val localIp = connectivityUtil.getWIFIIP()
                val webPort = config.webPort
                val joinMsg = Join("$localIp:$webPort")
                val joinMsgStr = (joinMsg as MulticastMessage).serialise()
                val data = DatagramPacket(joinMsgStr.toByteArray(), joinMsgStr.length, broadcastAddress, config.port)
                theSocket!!.send(data)
                log.d("sendBroadcast .. done")
            } catch (e: Exception) {
                log.e("sendBroadcast err", e)
            }
        }
    }

    override fun close() {
        isKeepGoing = false
        if (theSocket != null && !theSocket!!.isClosed) {
            // TODO note we might need to send a local exit message to stop blocking ...
            try {
                val localIp = connectivityUtil.getWIFIIP()
                val webPort = config.webPort
                val group = InetAddress.getByName(config.ip)
                val closeMsg = Close("$localIp:$webPort")
                val closeMsgStr = (closeMsg as MulticastMessage).serialise()
                val data = DatagramPacket(closeMsgStr.toByteArray(), closeMsgStr.length, group, config.port)
                theSocket!!.send(data)
                log.d("multi closing: send lastcall")
                val local = InetAddress.getByName("localhost")
                val data1 = DatagramPacket("".toByteArray(), 0, local, config.port)
                theSocket!!.send(data1)
                log.d("multi closing: ")
                theSocket!!.close()
            } catch (e: Exception) {
                log.e("multi close ex: ", e)
            }
        }
    }


    // region JSON serializer
    ////////////////////////////////////////////////////////////////////////////

    fun Join.serialise() = wifiJsonSerializer.encodeToString(Join.serializer(), this)
    fun deserialiseJoin(json: String) = wifiJsonSerializer.decodeFromString(Join.serializer(), json)


    fun Ping.serialise() = wifiJsonSerializer.encodeToString(Ping.serializer(), this)
    fun deserialisePing(json: String) = wifiJsonSerializer.decodeFromString(Ping.serializer(), json)


    fun Close.serialise() = wifiJsonSerializer.encodeToString(Close.serializer(), this)
    fun deserialiseClose(json: String) = wifiJsonSerializer.decodeFromString(Close.serializer(), json)
    fun MulticastMessage.serialise() = wifiJsonSerializer.encodeToString(MulticastMessage.serializer(), this)
    fun deserialiseMulti(json: String) = wifiJsonSerializer.decodeFromString(MulticastMessage.serializer(), json)


    //private val wifiDomainClassDiscriminator = "class"
    private val wifiSerializersModule = SerializersModule {
        mapOf(
            MulticastMessage::class to MulticastMessage.serializer(),
            Close::class to Close.serializer(),
            Join::class to Join.serializer(),
            Ping::class to Ping.serializer(),
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
        //classDiscriminator = wifiDomainClassDiscriminator// property added when base domain type is use (see ResponseDomain)
        serializersModule = wifiSerializersModule
    }
    //endregion
}