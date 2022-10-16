package uk.co.sentinelweb.cuer.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class ScreenStateReceiver(
    private val log: LogWrapper,
    context: Context,
) : BroadcastReceiver() {

    var isScreenOn = true
        private set

    var screenOffCallback: (() -> Unit)? = null

    val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager

    init {
        log.tag(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            isScreenOn = false
            screenOffCallback?.invoke()
            log.d("ACTION_SCREEN_OFF")
        } else if (intent.action == Intent.ACTION_SCREEN_ON) {
            isScreenOn = true
            log.d("ACTION_SCREEN_ON")
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(this, filter)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }

    fun isScreenLocked(): Boolean {
        return powerManager.isInteractive
    }
}
