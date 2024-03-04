package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color

class NotificationWrapper constructor(private val app: Application) {

    fun createChannelId(channelId: String, channelName: String) =
        createNotificationChannel(channelId, channelName)

    @Suppress("SameParameterValue")
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(chan)
        return channelId
    }
}