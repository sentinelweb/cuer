package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color

class NotificationWrapper constructor(private val app: Application) {

    fun createChannelId(channelId: String, channelName: String, importance: Int = NotificationManager.IMPORTANCE_LOW) =
        createNotificationChannel(channelId, channelName, importance)

    @Suppress("SameParameterValue")
    private fun createNotificationChannel(channelId: String, channelName: String, importance: Int): String {
        val chan = NotificationChannel(
            channelId,
            channelName,
            importance
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(chan)
        return channelId
    }
}
