package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.ActivityManager
import android.app.Application
import android.app.Service.*
import android.content.Context.ACTIVITY_SERVICE
import uk.co.sentinelweb.cuer.net.mappers.TimeStampMapper


class ServiceWrapper(
    private val app: Application,
    private val timeStampMapper: TimeStampMapper
) {

    data class Data constructor(
        val isRunning: Boolean = false,
        val isForeground: Boolean = false,
        val isStarted: Boolean = false,
        val activeSince: String = "Invalid",
        val flagContinuation: Boolean = false,
        val flagRedelivery: Boolean = false,
        val flagRetry: Boolean = false,
        val flagNotSticky: Boolean = false,
        val flagRedeliverIntent: Boolean = false,
        val flagSticky: Boolean = false,
        val flagStickyCompat: Boolean = false
    )

    fun getServiceData(serviceName: String): Data {
        val am = app.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val l = am.getRunningServices(50)
        val i: Iterator<ActivityManager.RunningServiceInfo> = l.iterator()
        while (i.hasNext()) {
            val runningServiceInfo = i.next()
            if (runningServiceInfo.service.className == serviceName) {
                return Data(
                    isRunning = true,
                    isForeground = runningServiceInfo.foreground,
                    isStarted = runningServiceInfo.started,
                    activeSince = timeStampMapper.mapDateTimeSimple(runningServiceInfo.activeSince),
                    flagContinuation = runningServiceInfo.flags and START_CONTINUATION_MASK == START_CONTINUATION_MASK,
                    flagNotSticky = runningServiceInfo.flags and START_NOT_STICKY == START_NOT_STICKY,
                    flagRedelivery = runningServiceInfo.flags and START_FLAG_REDELIVERY == START_FLAG_REDELIVERY,
                    flagRetry = runningServiceInfo.flags and START_FLAG_RETRY == START_FLAG_RETRY,
                    flagRedeliverIntent = runningServiceInfo.flags and START_REDELIVER_INTENT == START_REDELIVER_INTENT,
                    flagSticky = runningServiceInfo.flags and START_STICKY == START_STICKY,
                    flagStickyCompat = runningServiceInfo.flags and START_STICKY_COMPATIBILITY == START_STICKY_COMPATIBILITY
                )
            }
        }
        return SERVICE_NOT_FOUND
    }

    companion object {
        val SERVICE_NOT_FOUND = Data()
    }
}