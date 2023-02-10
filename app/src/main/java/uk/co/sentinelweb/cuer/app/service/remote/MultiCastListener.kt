package uk.co.sentinelweb.cuer.app.service.remote

import android.app.Service
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
//import co.uk.sentinelweb.mypod.v2.Globals
//import co.uk.sentinelweb.mypod.v2.util.ConnectivityUtil
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import org.koin.core.context.GlobalContext
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import java.io.IOException
import java.net.*
import java.nio.charset.Charset

class MultiCastListener(
    private val service: Service,
    private val ip: String = MULTICAST_IP_DEF,
    private val port: Int = MULTICAST_PORT_DEF,
    private val webPort: Int = SERVERPORT_DEF,
    private val log: LogWrapper = GlobalContext.get().get()
) {//: Thread("MultiCastListener") {

    var localIP: String? = null

    private var isKeepGoing = true
    private var theSocket: MulticastSocket? = null
    var recieveListener: ((String) -> Unit)? = null
    var startListener: (() -> Unit)? = null
    private lateinit var broadcastAddress: InetAddress

    init {
        log.tag(this)
        localIP = getWIFIIP(service) //ConnectivityUtil.getWIFIIP(service)
    }


    fun erun() {
        try {
            isKeepGoing = true
            broadcastAddress = InetAddress.getByName(ip)
            theSocket = MulticastSocket(port)
            theSocket!!.joinGroup(broadcastAddress)
            sendBroadcast()
            val buffer = ByteArray(10 * 1024)
            val data1 = DatagramPacket(buffer, buffer.size)
            log.d( "multi start: addr:$broadcastAddress")
            startListener?.invoke()
            while (isKeepGoing) {
                theSocket!!.receive(data1) // blocks
                val msg = String(buffer, 0, data1.length, Charset.defaultCharset())
                log.d( "multi Received: $msg")
                if (recieveListener != null) {
                    recieveListener?.invoke(msg)
                }
            }
        } catch (e: IOException) {
            log.e( e.toString(), e)
        }
        log.d( "exit")
    }

    @Serializable data class Join(val join: String?)// todo nonnull
    fun Join.serialise() = wifiJsonSerializer.encodeToString(Join.serializer(), this)

    fun sendBroadcast() {
        if (theSocket != null && !theSocket!!.isClosed) {
            try {
                //val closeMsg = HashMap<String, String>()
                //closeMsg["join"] = "$localIP:$webPort"
                val join = Join("$localIP:$webPort")
                val joinMsgStr = join.serialise()//: String = jd.asJSON(closeMsg).toString()
                val data = DatagramPacket(joinMsgStr.toByteArray(), joinMsgStr.length, broadcastAddress, port)
                theSocket!!.send(data)
                log.d( "sendBroadcast .. done")
            } catch (e: Exception) {
                log.e( "sendBroadcast err", e)
            }
        }
    }

//    fun setRecieveListener(_recieveListener: ((String) -> Unit)?) {
//        this._recieveListener = _recieveListener
//    }

    @Serializable data class Close(val close: String?)// todo nonnull
    fun Close.serialise() = wifiJsonSerializer.encodeToString(Close.serializer(), this)

    fun close() {
        isKeepGoing = false
        if (theSocket != null && !theSocket!!.isClosed) {
            // TODO note we might need to send a local exit message to stop blocking ...
//            object : Thread("MultiCastClose") {
//                fun erun() {
                    try {
                        val group = InetAddress.getByName(ip)
                        // send exit message
                        //val closeMsg = HashMap<String, String?>()
//                        closeMsg["close"] = localIP
//                        val closeMsgStr: String = jd.asJSON(closeMsg).toString()
                        val closeMsgStr = Close(localIP).serialise()//mapOf("close")//wifiSerializersModule.serializer<>()
                        val data = DatagramPacket(closeMsgStr.toByteArray(), closeMsgStr.length, group, port)
                        theSocket!!.send(data)
                        log.d( "multi closing: send lastcall")
                        val local = InetAddress.getByName("localhost")
                        val data1 = DatagramPacket("".toByteArray(), 0, local, port)
                        theSocket!!.send(data1)
                        log.d( "multi closing: ")
                        theSocket!!.close()
                    } catch (e: Exception) {
                        log.e( "multi close ex: ", e)
                        //Toast.makeText(service, "MultiCast close:"+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
//                }
//            }.start()
        }
    }

    // region ConnectivityUtil
    fun getWIFIIP(context: Context): String? {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity == null) {
            //Log.w(Globals.TAG, "Couldn't get connectivity manager");
        } else {
            val info = connectivity.activeNetworkInfo
            if (info != null) {
                if (info.type == ConnectivityManager.TYPE_WIFI) { // need to check the type returned for wifi.
                    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    val ipAddress = wifiInfo.ipAddress
                    return (ipAddress and 0xFF).toString() + "." + (ipAddress shr 8 and 0xFF) + "." + (ipAddress shr 16 and 0xFF) + "." + (ipAddress shr 24 and 0xFF)
                } else if (isNonMobile(info)) {
                    return getLocalIpAddress()
                }
            } else {
                return null
            }
        }
        return null
    }

    fun getWIFIID(context: Context): String? {
        var connectivity: ConnectivityManager? = null
        try {
            connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        } catch (e: NullPointerException) {
            /* Sometime we get an IConnectivityMannager not found */
        }
        if (connectivity == null) {
            log.d("Couldn't get connectivity manager")
        } else {
            val info = connectivity.activeNetworkInfo
            if (info != null) {
                if (info.type == ConnectivityManager.TYPE_WIFI) { // need to check the type returned for wifi.
                    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    var ssid = wifiInfo.ssid
                    if (ssid != null && ssid.length > 0 && ssid[0] == '"' && ssid.length > 2) {
                        ssid = ssid.substring(1, ssid.length - 1)
                    }
                    if ("" != ssid.trim()) return ssid
                }
            } else {
                return null
            }
        }
        return null
    }

    fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        return inetAddress.hostAddress.toString()
                    }
                }
            }
        } catch (ex: SocketException) {
            log.e("getLocalIpAddress", ex)
        }
        return null
    }

    fun toUnsignedByte(intVal: Int): Byte {
        val byteVal: Byte
        byteVal = if (intVal > 127) {
            val temp = intVal - 256
            temp.toByte()
        } else {
            intVal.toByte()
        }
        return byteVal
    }


    fun isNonMobileAvailable(context: Context): Boolean {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity == null) {
            //Log.w(Globals.TAG, "Couldn't get connectivity manager");
        } else {
            val info = connectivity.activeNetworkInfo
            return isNonMobile(info)
        }
        return false
    }

    private fun isNonMobile(info: NetworkInfo?): Boolean {
        return if (info != null) {
            !(info.type == ConnectivityManager.TYPE_MOBILE || info.type == ConnectivityManager.TYPE_MOBILE_DUN || info.type == ConnectivityManager.TYPE_MOBILE_HIPRI || info.type == ConnectivityManager.TYPE_MOBILE_MMS || info.type == ConnectivityManager.TYPE_MOBILE_SUPL || info.type == ConnectivityManager.TYPE_WIMAX)
        } else {
            false
        }
    }
    // endregion

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



    companion object{

        const val SERVERPORT_DEF = 4444
        const val MULTICAST_PORT_DEF = 4445
        const val MULTICAST_IP_DEF = "224.0.0.1"

    }

}