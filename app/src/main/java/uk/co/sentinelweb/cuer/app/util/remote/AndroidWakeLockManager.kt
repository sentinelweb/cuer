package uk.co.sentinelweb.cuer.app.util.remote

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager
import uk.co.sentinelweb.cuer.remote.server.WakeLockManager

class AndroidWakeLockManager(private val context: Context) : WakeLockManager {
    private var wifiLock: WifiManager.WifiLock? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun acquireWakeLock() {
        releaseWakeLock()
        // Acquire power manager and wifi manager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Acquire wakelock
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
            .apply { acquire() }

        // Acquire Wi-Fi lock
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, TAG)
            .apply { acquire() }
    }

    override fun releaseWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.also { it.release() }
        wifiLock?.takeIf { it.isHeld }?.also { it.release() }
    }

    companion object {
        private const val TAG = "Cuer:WakeLock"
    }
}
