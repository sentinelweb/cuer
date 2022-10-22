package uk.co.sentinelweb.cuer.app.receiver

import android.app.Application
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper


class ScreenStateReceiver(
) : KoinComponent, BroadcastReceiver() {

    private val log: LogWrapper by inject()
    private val application: Application by inject()

    var isScreenOn = true
        private set

    val isLocked: Boolean
        get() = keyguardManager.isDeviceLocked

    val isKeyguardActive: Boolean
        get() = keyguardManager.isKeyguardLocked

    val screenOffCallbacks: MutableList<(() -> Unit)> = mutableListOf()
    val screenOnCallbacks: MutableList<(() -> Unit)> = mutableListOf()
    val unlockCallbacks: MutableList<(() -> Unit)> = mutableListOf()

    var keyguardManager = application.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    init {
        log.tag(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            log.d("ACTION_SCREEN_OFF")
            isScreenOn = false
            screenOffCallbacks.map { it() }
        } else if (intent.action == Intent.ACTION_SCREEN_ON) {
            log.d("ACTION_SCREEN_ON")
            isScreenOn = true
            screenOnCallbacks.map { it() }
        } else if (intent.action == Intent.ACTION_USER_PRESENT) {
            log.d("ACTION_USER_PRESENT")
            isScreenOn = true
            unlockCallbacks.map { it() }
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        context.registerReceiver(this, filter)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }

}
