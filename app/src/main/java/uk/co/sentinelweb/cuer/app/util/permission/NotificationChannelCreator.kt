package uk.co.sentinelweb.cuer.app.util.permission

import android.app.NotificationManager
import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.service.cast.CastService
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerService
import uk.co.sentinelweb.cuer.app.service.update.UpdateService
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingNotification
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerService
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper

class NotificationChannelCreator(
    private val appState: CuerAppState,
    private val notificationWrapper: NotificationWrapper,
) {
    fun create() {
        appState.castNotificationChannelId = notificationWrapper.createChannelId(
            CastService.CHANNEL_ID,
            CastService.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MAX
        )

        appState.floatingNotificationChannelId = notificationWrapper.createChannelId(
            FloatingPlayerService.CHANNEL_ID,
            FloatingPlayerService.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MAX
        )

        appState.remoteNotificationChannelId = notificationWrapper.createChannelId(
            RemoteServerService.CHANNEL_ID,
            RemoteServerService.CHANNEL_NAME
        )

        appState.updateNotificationChannelId = notificationWrapper.createChannelId(
            UpdateService.CHANNEL_ID,
            UpdateService.CHANNEL_NAME
        )

        appState.upcomingNotificationChannelId = notificationWrapper.createChannelId(
            UpcomingNotification.CHANNEL_ID,
            UpcomingNotification.CHANNEL_NAME
        )
    }
}
