package uk.co.sentinelweb.cuer.app.util.permission

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class NotificationPermissionCheck(
    private val context: Context,
    private val log: LogWrapper
) {

    init {
        log.tag(this)
    }

    fun isNotificationsEnabled(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun createChannel(channelId: String, channelName: String) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun checkPermission(channelId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = notificationManager.getNotificationChannel(channelId)
        val name = channel.name
        val isEnabled = channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    private fun deleteChannel(channelId: String) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.deleteNotificationChannel(channelId)
    }

    private fun isChannelEnabled(channelId: String): Boolean {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel: NotificationChannel? = notificationManager.getNotificationChannel(channelId)
        return channel?.importance != NotificationManager.IMPORTANCE_NONE
    }
}