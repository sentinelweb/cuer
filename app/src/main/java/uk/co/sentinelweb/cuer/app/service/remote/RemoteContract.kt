package uk.co.sentinelweb.cuer.app.service.remote

import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
//import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter

class RemoteContract {

    interface Service {
        val isServerStarted: Boolean
        val address: String?
        fun stopSelf()
    }

    interface Controller {
        val isServerStarted: Boolean
        val address: String?
        fun initialise()
        fun handleAction(action: String?)
        fun destroy()
    }

    interface Notification {

        interface External {
            fun updateNotification(address: String)
            fun handleAction(action: String?)
            fun destroy()
        }

        interface Controller

        interface View {
            fun showNotification(address: String)
            fun stopSelf()
        }

        data class State constructor(
            val isStarted: Boolean = false
        )
    }

    companion object {
        val serviceModule = module {
            single { RemoteServiceManager(androidApplication()) }
            scope(named<RemoteService>()) {
                scoped<Controller> {
                    RemoteServiceController(
                        service = get<RemoteService>(),
                        notification = get(),
                        webServer = get(),
                        coroutines = get(),
                        log = get()
                    )
                }
                scoped<Notification.External> {
                    RemoteNotificationController(
                        view = get(),
                        state = get()
                    )
                }
                scoped<Notification.View> {
                    RemoteNotification(
                        service = get<RemoteService>(),
                        appState = get(),
                        timeProvider = get(),
                        log = get(),
                        res = get()
                    )
                }
                scoped { Notification.State() }
            }
            factory<RemoteDatabaseAdapter> {
                AppRemoteDatabaseAdapter(
                    playlistOrchestrator = get(),
                    playlistItemOrchestrator = get(),
                    addLinkOrchestrator = get()
                )
            }
            // test injection
//        factory<RemoteDatabaseAdapter> {
//            TestDatabase.hardcoded()
//        }
        }
    }
}