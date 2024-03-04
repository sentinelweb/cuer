package uk.co.sentinelweb.cuer.app.service.update

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.update.UpdateServiceContract.Notification.Model
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity

class UpdateServiceNotification(
    private val service: UpdateService,
    private val appState: CuerAppState,
) : UpdateServiceContract.Notification.View {
    override fun showNotification(summary: Model) {
        service.startForeground(
            FOREGROUND_ID,
            buildNotification(summary)
        )
    }

    private fun buildNotification(
        model: Model
    ): Notification =
        when (model.type) {
            Model.Type.RESULT -> {
                val inboxStyle = NotificationCompat.InboxStyle()
                    .also { style -> model.items.forEach { style.addLine(it.title) } }

                val intent = PendingIntent.getActivity(
                    service,
                    0,
                    Intent(service, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )

                NotificationCompat.Builder(service, appState.updateNotificationChannelId!!)
                    .setSmallIcon(R.drawable.ic_refresh)
                    .setContentTitle("${model.itemsUpdated} items updated")
                    .setContentText("Tap to view the media items")
                    .setStyle(inboxStyle)
                    .setContentIntent(intent)
                    .build()
            }

            Model.Type.STATUS -> {
                NotificationCompat.Builder(service, appState.updateNotificationChannelId!!)
                    .setSmallIcon(R.drawable.ic_refresh)
                    .setContentTitle(model.status)
                    .setContentText("Some text .. remove")
                    .build()
            }
        }


    override fun stopSelf() {
        service.stopSelf()
    }

    companion object {
        const val FOREGROUND_ID = 34527
    }
}