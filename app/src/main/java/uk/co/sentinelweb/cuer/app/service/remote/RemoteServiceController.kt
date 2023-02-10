package uk.co.sentinelweb.cuer.app.service.remote

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.remote.server.RemoteServer
//import uk.co.sentinelweb.cuer.remote.server.RemoteServer
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

// todo remove for remote
//interface RemoteServer {
//    val port: Int
//    fun fullAddress(ip: String): String
//    val isRunning: Boolean
//    fun start()
//    fun stop()
//}
//
//class EmptyRemoteServer : RemoteServer {
//    override val port: Int
//        get() = 2345
//
//    override fun fullAddress(ip: String): String {
//        return "fulladdress"
//    }
//
//    override val isRunning: Boolean
//        get() = false
//
//    override fun start() {}
//
//    override fun stop() {}
//}

class RemoteServiceController constructor(
    private val service: RemoteService,
    private val notification: RemoteContract.Notification.External,
    private val webServer: RemoteServer,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper
) : RemoteContract.Controller {

    init {
        log.tag(this)
    }

    private var serverJob: Job? = null
    private lateinit var multi: MultiCastListener


    override val isServerStarted: Boolean
        get() = webServer.isRunning

    override val address: String?
        get() = let {
            if (webServer.isRunning) wifiIpAddress(service)?.let { webServer.fullAddress(it) }
            else null
        }.apply { log.d("address: $this ${webServer.isRunning}") }

    override fun initialise() {
        notification.updateNotification("x")
        serverJob?.cancel()
        serverJob = coroutines.ioScope.launch {
            val address = wifiIpAddress(service)?.let { webServer.fullAddress(it) }
            withContext(coroutines.Main) {
                address?.apply { notification.updateNotification(this) }
            }
            webServer.start()
        }
        coroutines.ioScope.launch {
            multi = MultiCastListener(service)
            multi.recieveListener = { msg ->
                log.d("multicast recieve: $msg")
            }
            multi.startListener = {
                log.d("multicast started")
                coroutines.ioScope.launch {
                    delay(50)
                    multi.sendBroadcast()
                }

            }
            multi.erun()
        }
    }

    override fun handleAction(action: String?) {
        notification.handleAction(action)
    }

    override fun destroy() {
        log.d("Controller destroy")
        serverJob?.cancel()
        serverJob = null
        webServer.stop()
        notification.destroy()
        coroutines.ioScope.launch {
            delay(50)
            multi.close()
        }

        log.d("Controller destroyed")
    }

    // https://stackoverflow.com/questions/16730711/get-my-wifi-ip-address-android
    private fun wifiIpAddress(context: Context): String? {
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