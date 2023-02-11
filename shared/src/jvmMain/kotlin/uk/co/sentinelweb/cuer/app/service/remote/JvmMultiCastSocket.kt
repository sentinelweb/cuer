package uk.co.sentinelweb.cuer.app.service.remote

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.nio.charset.Charset

class JvmMultiCastSocket(
    private val config: MultiCastSocketContract.Config,
    private val log: LogWrapper,
    private val connectivityUtil: ConnectivityWrapper
) : MultiCastSocketContract {

    private var isKeepGoing = true
    private var theSocket: MulticastSocket? = null
    private lateinit var broadcastAddress: InetAddress

    override var recieveListener: ((String) -> Unit)? = null
    override var startListener: (() -> Unit)? = null

    init {
        log.tag(this)
    }


    override fun runSocketListener() {
        try {
            isKeepGoing = true
            broadcastAddress = InetAddress.getByName(config.ip)
            theSocket = MulticastSocket(config.port)
            theSocket!!.joinGroup(broadcastAddress)
            sendBroadcast()
            val buffer = ByteArray(10 * 1024)
            val data1 = DatagramPacket(buffer, buffer.size)
            log.d("multi start: addr:$broadcastAddress")
            startListener?.invoke()
            while (isKeepGoing) {
                theSocket!!.receive(data1) // blocks
                val msg = String(buffer, 0, data1.length, Charset.defaultCharset())
                log.d("multi Received: $msg")
                if (recieveListener != null) {
                    recieveListener?.invoke(msg)
                }
            }
        } catch (e: IOException) {
            log.e(e.toString(), e)
        }
        log.d("exit")
    }

    @Serializable
    data class Join(val join: String?)// todo nonnull

    fun Join.serialise() = wifiJsonSerializer.encodeToString(Join.serializer(), this)

    override fun sendBroadcast() {
        if (theSocket != null && !theSocket!!.isClosed) {
            try {
                //val closeMsg = HashMap<String, String>()
                //closeMsg["join"] = "$localIP:$webPort"
                val localIp = connectivityUtil.getWIFIIP()
                val webPort = config.webPort
                val join = Join("$localIp:$webPort")
                val joinMsgStr = join.serialise()//: String = jd.asJSON(closeMsg).toString()
                val data = DatagramPacket(joinMsgStr.toByteArray(), joinMsgStr.length, broadcastAddress, config.port)
                theSocket!!.send(data)
                log.d("sendBroadcast .. done")
            } catch (e: Exception) {
                log.e("sendBroadcast err", e)
            }
        }
    }

    @Serializable
    data class Close(val close: String?)// todo nonnull

    fun Close.serialise() = wifiJsonSerializer.encodeToString(Close.serializer(), this)

    override fun close() {
        isKeepGoing = false
        if (theSocket != null && !theSocket!!.isClosed) {
            // TODO note we might need to send a local exit message to stop blocking ...
//            object : Thread("MultiCastClose") {
//                fun erun() {
            try {
                val localIp = connectivityUtil.getWIFIIP()
                val webPort = config.webPort
                val join = Join("$localIp:$webPort")
                val group = InetAddress.getByName(config.ip)
                // send exit message
                //val closeMsg = HashMap<String, String?>()
//                        closeMsg["close"] = localIP
//                        val closeMsgStr: String = jd.asJSON(closeMsg).toString()
                val closeMsgStr = Close(localIp).serialise()//mapOf("close")//wifiSerializersModule.serializer<>()
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
                //Toast.makeText(service, "MultiCast close:"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
//                }
//            }.start()
        }
    }


    // region JSON serializer
    ////////////////////////////////////////////////////////////////////////////
    //private val wifiDomainClassDiscriminator = "domainType"
    private val wifiSerializersModule = SerializersModule {
        mapOf(
            Close::class to Close.serializer(),
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