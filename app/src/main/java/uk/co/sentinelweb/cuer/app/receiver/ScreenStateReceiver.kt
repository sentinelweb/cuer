package uk.co.sentinelweb.cuer.app.receiver

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter


class ScreenStateReceiver : BroadcastReceiver() {

    private lateinit var context: Context

    var isScreenOn = true
        private set

    val isLocked: Boolean
        get() = keyguardManager.isDeviceLocked

    val isKeyguardActive: Boolean
        get() = keyguardManager.isKeyguardLocked

    val screenOffCallbacks: MutableList<(() -> Unit)> = mutableListOf()
    val screenOnCallbacks: MutableList<(() -> Unit)> = mutableListOf()
    val unlockCallbacks: MutableList<(() -> Unit)> = mutableListOf()

    private val keyguardManager: KeyguardManager
        get() = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            isScreenOn = false
            screenOffCallbacks.map { it() }
        } else if (intent.action == Intent.ACTION_SCREEN_ON) {
            isScreenOn = true
            screenOnCallbacks.map { it() }
        } else if (intent.action == Intent.ACTION_USER_PRESENT) {
            isScreenOn = true
            unlockCallbacks.map { it() }
        }
    }

    fun register(context: Context) {
        this.context = context
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        context.registerReceiver(this, filter)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }

    fun requestUnlockKeyGuard() {
        // https://stackoverflow.com/questions/3873659/android-how-can-i-get-the-current-foreground-activity-from-a-service
//        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val cuerActivityComponentName = am.appTasks
//            .first { it.taskInfo.topActivity?.packageName == this.context.packageName }
//            ?.taskInfo?.topActivity
        // want to have the currnet activity instance here might need to use the
        //     registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
        // from the above link to keep a reference
        // then
        // keyguardManager.requestDismissKeyguard(activity)
    }

}
