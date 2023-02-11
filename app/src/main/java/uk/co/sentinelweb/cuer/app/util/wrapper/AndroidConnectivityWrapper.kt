package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import java.math.BigInteger
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteOrder

class AndroidConnectivityWrapper constructor(
    private val context: Context,
    private val log: LogWrapper
) : ConnectivityWrapper {

    init {
        log.tag(this)
    }

    private val cm: ConnectivityManager by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    override fun isConnected(): Boolean {
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    override fun isMetered(): Boolean = cm.isActiveNetworkMetered()

    override fun getLocalIpAddress(): String? {
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

    private fun toUnsignedByte(intVal: Int): Byte {
        val byteVal: Byte
        byteVal = if (intVal > 127) {
            val temp = intVal - 256
            temp.toByte()
        } else {
            intVal.toByte()
        }
        return byteVal
    }


    override fun isNonMobileAvailable(): Boolean = cm.activeNetworkInfo?.let { isNonMobile(it) } ?: false


    private fun isNonMobile(info: NetworkInfo?): Boolean =
        info?.run {
            !(type == ConnectivityManager.TYPE_MOBILE
                    || type == ConnectivityManager.TYPE_MOBILE_DUN
                    || type == ConnectivityManager.TYPE_MOBILE_HIPRI
                    || type == ConnectivityManager.TYPE_MOBILE_MMS
                    || type == ConnectivityManager.TYPE_MOBILE_SUPL
                    || type == ConnectivityManager.TYPE_WIMAX)
        } ?: false

    override fun getWIFIID(): String? {
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

    override fun getWIFIIP(): String? {
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


    // https://stackoverflow.com/questions/16730711/get-my-wifi-ip-address-android
    override fun wifiIpAddress(): String? {
        val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager
        var ipAddress = wifiManager.connectionInfo.ipAddress

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress)
        }
        val ipByteArray: ByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
        val ipAddressString: String?
        ipAddressString = try {
            InetAddress.getByAddress(ipByteArray).getHostAddress()
        } catch (ex: UnknownHostException) {
            log.e("Unable to get host address.")
            null
        }
        return ipAddressString
    }
}