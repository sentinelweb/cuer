package uk.co.sentinelweb.cuer.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.service.remote.WifiStartChecker
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider.WifiState


class WifiStateReceiver(
    private val context: Context,
    private val log: LogWrapper,
) : BroadcastReceiver(), WifiStateProvider, KoinComponent {

    private val wifiStartChecker: WifiStartChecker by inject()

    private val _wifiStateFlow: MutableStateFlow<WifiState> = MutableStateFlow(WifiState())
    override val wifiState: WifiState
        get() = _wifiStateFlow.value.also { log.d("wifiState.value = $it") }

    override val wifiStateFlow = _wifiStateFlow

    init {
        log.tag(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.action)
            || WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(intent.action)
        ) {
            val netInfo: NetworkInfo? = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
            //dumpNetworkData(intent, context)
            if (netInfo?.type == ConnectivityManager.TYPE_WIFI) {
                updateWifiInfo()
                    .also { wifiStartChecker.checkToStartServer(_wifiStateFlow.value) }
            }
        }
    }

    override fun register() {
        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION)
        context.registerReceiver(this, filter)
    }

    override fun unregister() {
        context.unregisterReceiver(this)
    }

    override fun updateWifiInfo() {
        queryWifiState()
            .also { _wifiStateFlow.value = it }
    }

    private fun queryWifiState(): WifiState {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.connectionInfo
            ?.takeIf { it.ipAddress != 0 } // hack but connectivity manager return the wrong connected state
            ?.run {
                val obscured = ssid == AndroidConnectivityWrapper.UNKNOWN_SSID
                WifiState(
                    isConnected = ipAddress != 0,
                    isObscured = obscured,
                    ssid = if (obscured) null else ssid.stripQuotes(),
                    ip = ipToString(),
                )
            } ?: WifiState()
    }


    private fun String.stripQuotes() =
        if (length > 0 && this[0] == '"' && length > 2) substring(1, length - 1) else this

    private fun WifiInfo.ipToString() =
        (ipAddress and 0xFF).toString() + "." + (ipAddress shr 8 and 0xFF) + "." + (ipAddress shr 16 and 0xFF) + "." + (ipAddress shr 24 and 0xFF)


    @RequiresApi(Build.VERSION_CODES.S)
    private fun dumpNetworkData(intent: Intent, context: Context) {
        val netInfo: NetworkInfo? = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
        log.d(netInfo?.run {
            "netInfo:\n" +
                    "isConnected:$isConnected\n" +
                    "isConnectedOrConnecting:$isConnectedOrConnecting\n" +
                    "isAvailable: $isAvailable\n" +
                    "type: $type\n" +
                    "typeName: $typeName\n" +
                    "subtype: $subtype\n" +
                    "subtypeName: $subtypeName\n" +
                    "extraInfo: $extraInfo\n" +
                    "reason: $reason\n" +
                    "isFailover: $isFailover\n" +
                    "state: $state\n"
        } ?: "no net info")

        //if (netInfo?.isConnected ?: false) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo? = wifiManager.connectionInfo
        log.d(wifiInfo?.run {
            val enabledState = when (wifiManager.wifiState) {
                1 -> "WIFI_STATE_DISABLED"
                0 -> "WIFI_STATE_DISABLING"
                3 -> "WIFI_STATE_ENABLED"
                2 -> "WIFI_STATE_ENABLING"
                4 -> "WIFI_STATE_UNKNOWN"
                else -> "WIFI_STATE_INVALID"
            }
            "wifiInfo:\n" +
                    "enabledState: $enabledState\n" +
                    "ssid: $ssid\n" +
                    "bssid: $bssid\n" +
                    "wifiStandard: $wifiStandard\n" + // api 30
                    "frequency: $frequency\n" +
                    "linkSpeed: $linkSpeed\n" +
                    "networkId: $networkId\n" +
                    "rssi: $rssi\n" +
                    "txLinkSpeedMbps: $txLinkSpeedMbps\n" + // api 29
                    "rxLinkSpeedMbps: $rxLinkSpeedMbps\n" + // api 29
                    "macAddress: $macAddress\n" +
                    "subscriptionId: $subscriptionId\n" + // api 31
                    "hiddenSSID: $hiddenSSID\n"
        } ?: "no wifi info")
        //}
    }

//    private fun ssidAndIp(): Pair<String?, String?>? {
//        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val wifiInfo: WifiInfo? = wifiManager.connectionInfo
//        return wifiInfo?.let {
//            it.ssid to wifiIpAddress()
//        }
//    }

//    override fun wifiIpAddress(): String? {
//        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        var ipAddress = wifiManager.connectionInfo.ipAddress
//
//        // Convert little-endian to big-endianif needed
//        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
//            ipAddress = Integer.reverseBytes(ipAddress)
//        }
//        val ipByteArray: ByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
//        val ipAddressString: String?
//        ipAddressString = try {
//            InetAddress.getByAddress(ipByteArray).getHostAddress()
//        } catch (ex: UnknownHostException) {
//            log.e("Unable to get host address.")
//            null
//        }
//        return ipAddressString
//    }

//            val statedChanged = WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())
//            val idChanged = WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(intent.getAction())
//            log.d("WIFI state changed: $statedChanged, id changed: $idChanged")

}
