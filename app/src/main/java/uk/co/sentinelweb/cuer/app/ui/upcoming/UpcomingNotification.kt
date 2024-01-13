package uk.co.sentinelweb.cuer.app.ui.upcoming

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class UpcomingNotification(
    notificationWrapper: NotificationWrapper,
    private val appState: CuerAppState,
    private val appContext: Application,
    private val timeProvider: TimeProvider,
    private val res: ResourceWrapper
) : UpcomingContract.View {
    init {
        appState.upcomingNotificationChannelId = notificationWrapper.createChannelId(
            CHANNEL_ID,
            CHANNEL_NAME
        )
    }

    override fun showNotification(item: PlaylistItemDomain) {
        val contentIntent = Intent(appContext, MainActivity::class.java)
        val contentPendingIntent: PendingIntent =
            PendingIntent.getActivity(appContext, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(
            appContext,
            appState.upcomingNotificationChannelId!!
        )
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_upcoming)
            .setContentTitle(res.getString(R.string.upcoming_notif_title))
            .setContentText(item.media.title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(timeProvider.currentTimeMillis())
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)

        val built = builder.build()
        val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(0, built)
    }

    companion object {
        //private var _instance: UpdateService? = null
        private const val CHANNEL_ID: String = "cuer_upcoming"
        private const val CHANNEL_NAME: String = "Cuer upcoming episodes"
        //fun instance(): UpdateService? = _instance
    }
}