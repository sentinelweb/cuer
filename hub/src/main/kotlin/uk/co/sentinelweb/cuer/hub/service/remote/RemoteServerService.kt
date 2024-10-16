package uk.co.sentinelweb.cuer.hub.service.remote

import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerServiceController
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource

class RemoteServerService(
    private val log: LogWrapper,
) : RemoteServerContract.Service, DesktopScopeComponent {

    override val scope: Scope = desktopScopeWithSource(this)

    private val controller: RemoteServerContract.Controller by scope.inject()

    init {
        log.tag(this)
        controller.initialise()
    }

    override val isServerStarted: Boolean
        get() = controller.isServerStarted

    override val localNode: LocalNodeDomain
        get() = controller.localNode

    override var stopListener: (() -> Unit)? = null

    override suspend fun multicastPing() {
        controller.multicastPing()
    }

    override fun stopSelf() {
        log.d("stop remote service")
        controller.destroy()
        scope.close()
        stopListener?.invoke()
    }

    companion object {
        val serviceModule = module {
            scope(named<RemoteServerService>()) {
                scoped<RemoteServerContract.Controller> {
                    RemoteServerServiceController(
                        notification = get(),
                        webServer = get(),
                        coroutines = get(),
                        log = get(),
                        multi = get(),
                        localRepo = get(),
                        remoteRepo = get(),
                        wakeLockManager = get(),
                        wifiStateProvider = get(),
                        service = get(),
                    )
                }
                scoped<RemoteServerContract.Notification.External> {
                    RemoteServerNotificationController(
                        view = get(),
                        state = get()
                    )
                }
                scoped<RemoteServerContract.Notification.View> {
                    RemoteServerNotification(
                        service = get<RemoteServerService>(),
                        timeProvider = get(),
                        log = get(),
                    )
                }
                scoped { RemoteServerContract.Notification.State() }
            }
        }
    }
}
