package uk.co.sentinelweb.cuer.hub.service.remote

import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.providers.getOS
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.hub.ui.common.notif.AppleScriptNotif

class RemoteServerNotification constructor(
    private val service: RemoteServerContract.Service,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper,
) : RemoteServerContract.Notification.View {

    init {
        log.tag(this)
    }

    override fun showNotification(content: String) {
        // todo make something for windows and linux
        if (getOS() == NodeDomain.DeviceType.MAC) {
            AppleScriptNotif.showNotification("Server status", content)
        }
    }

    override fun stopSelf() {
        service.stopSelf()
    }
}