package uk.co.sentinelweb.cuer.app.util.permission

import uk.co.sentinelweb.cuer.app.CuerAppState
import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastService
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerService
import uk.co.sentinelweb.cuer.app.service.update.UpdateService
import uk.co.sentinelweb.cuer.app.ui.upcoming.UpcomingNotification
import uk.co.sentinelweb.cuer.app.util.wrapper.NotificationWrapper

class NotificationChannelCreator(
    private val appState: CuerAppState,
    private val notificationWrapper: NotificationWrapper,
) {
    fun create() {
        appState.castNotificationChannelId = notificationWrapper.createChannelId(
            YoutubeCastService.CHANNEL_ID,
            YoutubeCastService.CHANNEL_NAME
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