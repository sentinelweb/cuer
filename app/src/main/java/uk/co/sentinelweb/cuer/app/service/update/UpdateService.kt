package uk.co.sentinelweb.cuer.app.service.update

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidApplication
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.extension.serviceScopeWithSource
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper


class UpdateService() : Service(), UpdateServiceContract.Service, AndroidScopeComponent {

    override val scope: Scope by serviceScopeWithSource()
    private val controller: UpdateServiceContract.Controller by scope.inject()
    private val log: LogWrapper by inject()

    override fun onCreate() {
        super.onCreate()
        log.tag(this)
        log.d("Update Service created")
        controller.initialise()
    }

    override fun onDestroy() {
        super.onDestroy()
        log.d("Update Service destroyed")
        controller.destroy()
        scope.close()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        controller.handleAction(intent?.action)
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID: String = "cuer_update_service"
        const val CHANNEL_NAME: String = "Cuer Update Service"

        val serviceModule = module {
            single<UpdateServiceContract.Manager> { UpdateServiceManager(androidApplication()) }
            scope(named<UpdateService>()) {
                scoped<UpdateServiceContract.Controller> {
                    UpdateServiceController(
                        coroutines = get(),
                        log = get(),
                        service = get(),
                        mediaUpdateFromPlatformUseCase = get(),
                        playlistItemOrchestrator = get(),
                        mediaOrchestrator = get(),
                        timeProvider = get(),
                        notification = get(),
                        connectivityWrapper = get()
                    )
                }
                scoped<UpdateServiceContract.Notification.External> {
                    UpdateServiceNotificationController(
                        view = get()
                    )
                }
                scoped<UpdateServiceContract.Notification.View> {
                    UpdateServiceNotification(
                        service = get<UpdateService>(),
                        appState = get()
                    )
                }
            }
        }
    }
}
