package uk.co.sentinelweb.cuer.app.service.update

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.service.update.UpdateServiceContract.Notification.Model
import uk.co.sentinelweb.cuer.app.service.update.UpdateServiceContract.Notification.Model.Type
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity

class UpdateServiceNotification(
    private val service: UpdateService,
    private val appState: CuerAppState,

) : UpdateServiceContract.Notification.View {
    override fun showNotification(summary: Model) {
        when (summary.type) {
            Type.RESULT -> {
                val nm = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(0, buildNotificationResult(summary))
            }

            Type.STATUS -> {
                service.startForeground(
                    FOREGROUND_ID,
                    buildNotificationStatus(summary)
                )
            }

            Type.ERROR -> {
                val nm = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(0, buildNotificationError(summary))
            }
        }
    }

    private fun buildNotificationStatus(model: Model) =
        NotificationCompat.Builder(service, appState.updateNotificationChannelId!!)
            .setSmallIcon(R.drawable.ic_refresh)
            .setContentTitle(model.status)
            .setContentText("Updating ...")
            .setOngoing(true)
            .build()

    private fun buildNotificationError(model: Model) =
        NotificationCompat.Builder(service, appState.updateNotificationChannelId!!)
            .setSmallIcon(R.drawable.ic_refresh)
            .setContentTitle(model.status)
            .setContentText("Check the error and try again")
            .build()

    private fun buildNotificationResult(model: Model): Notification {
        val inboxStyle = NotificationCompat.InboxStyle()
            .also { style -> model.items.forEach { style.addLine(it.title) } }

        val intent = PendingIntent.getActivity(
            service,
            0,
            Intent(service, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(service, appState.updateNotificationChannelId!!)
            .setSmallIcon(R.drawable.ic_refresh)
            .setContentTitle("${model.itemsUpdated} items updated")
            .setContentText("Tap to view the media items")
            .setStyle(inboxStyle)
            .setContentIntent(intent)
            .setOngoing(false)
            .build()
    }


    override fun stopSelf() {
        service.stopSelf()
    }

    companion object {
        const val FOREGROUND_ID = 34527
    }
}