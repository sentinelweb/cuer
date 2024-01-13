package uk.co.sentinelweb.cuer.app.receiver

import android.app.Application
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
import org.koin.core.context.GlobalContext
import uk.co.sentinelweb.cuer.app.service.remote.WifiStartChecker
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider.WifiState


class WifiStateReceiver(
    private val connectivityWrapper: ConnectivityWrapper,
    private val log: LogWrapper,
    private val wifiStartChecker: WifiStartChecker,
) : BroadcastReceiver(), WifiStateProvider {

    private val _wifiStateFlow: MutableStateFlow<WifiState> = MutableStateFlow(WifiState())
    override val wifiStateFlow = _wifiStateFlow

    init {
        log.tag(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.action)
            || WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(intent.action)
        ) {
//            val statedChanged = WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())
//            val idChanged = WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(intent.getAction())
//            log.d("WIFI state changed: $statedChanged, id changed: $idChanged")
            val netInfo: NetworkInfo? = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
            //dumpNetworkData(intent, context)
            if (netInfo?.type == ConnectivityManager.TYPE_WIFI) {
                connectivityWrapper.getWifiInfo()
                    .also { wifiStartChecker.checkToStartServer(it) }
                    .also { _wifiStateFlow.value = it }
            }
        }
    }

    override fun register() {
        val appContext: Application = GlobalContext.get().get()
        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION)
        appContext.registerReceiver(this, filter)
    }

    override fun unregister() {
        val appContext: Application = GlobalContext.get().get()
        appContext.unregisterReceiver(this)
    }

    override fun updateWifiInfo() {
        connectivityWrapper.getWifiInfo()
            .also { _wifiStateFlow.value = it }
    }

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

}
