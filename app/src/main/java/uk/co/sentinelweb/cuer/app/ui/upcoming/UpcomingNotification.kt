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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.SOURCE
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider.Companion.toInstant
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class UpcomingNotification(
    private val appState: CuerAppState,
    private val appContext: Application,
    private val timeProvider: TimeProvider,
    private val res: ResourceWrapper,
    private val log: LogWrapper,
) : UpcomingContract.View {
    init {
        log.tag(this)
    }

    override fun showNotification(item: PlaylistItemDomain) {
        val contentIntent = Intent(appContext, MainActivity::class.java).apply {
            putExtra(NavigationModel.Target.KEY, NavigationModel.Target.PLAYLIST_ITEM.toString())
            putExtra(SOURCE.toString(), OrchestratorContract.Source.LOCAL.toString())
            putExtra(NavigationModel.Param.PLAYLIST_ITEM.toString(), item.serialise())
        }
        val contentPendingIntent: PendingIntent =
            PendingIntent.getActivity(appContext, 0, contentIntent, PendingIntent.FLAG_MUTABLE)
        val now = timeProvider.instant()
        val minsUntil = item.media.broadcastDate
            ?.let { (it.toInstant() - now).inWholeMinutes }
            ?.toString()
            ?: "?"
        val builder = NotificationCompat.Builder(
            appContext,
            appState.upcomingNotificationChannelId!!
        )
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_upcoming)
            .setContentTitle(res.getString(R.string.upcoming_notif_title, minsUntil))
            .setContentText(item.media.title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(timeProvider.currentTimeMillis())
            .setContentIntent(contentPendingIntent)

        val built = builder.build()
        val nm = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(0, built)
    }

    companion object {
        const val CHANNEL_ID: String = "cuer_upcoming"
        const val CHANNEL_NAME: String = "Cuer upcoming episodes"
    }
}