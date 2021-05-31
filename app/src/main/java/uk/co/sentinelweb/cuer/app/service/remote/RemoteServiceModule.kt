package uk.co.sentinelweb.cuer.app.service.remote

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter
import uk.co.sentinelweb.cuer.remote.server.database.TestDatabase

object RemoteServiceModule {

    val serviceModule = module {
        factory { RemoteServiceManager(androidApplication()) }
        scope(named<RemoteService>()) {
            scoped<RemoteContract.Controller> {
                RemoteServiceController(
                    service = getSource(),
                    notification = get(),
                    webServer = get(),
                    coroutines = get()
                )
            }
            scoped<RemoteContract.Notification.External> {
                RemoteNotificationController(
                    view = get(),
                    state = get()
                )
            }
//            scoped {
//                get<RemoteContract.Notification.Controller>()
//            }
            scoped<RemoteContract.Notification.View> {
                RemoteNotification(
                    service = getSource(),
                    appState = get(),
                    timeProvider = get(),
                    log = get()
                )
            }
            scoped { RemoteContract.Notification.State() }


        }
        // test injection
        factory<RemoteDatabaseAdapter> {
            TestDatabase.hardcoded()
        }
    }
}