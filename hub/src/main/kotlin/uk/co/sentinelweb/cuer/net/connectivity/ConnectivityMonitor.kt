package uk.co.sentinelweb.cuer.net.connectivity

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider

class ConnectivityMonitor(
    connectivityCheckTimer: ConnectivityCheckTimer,
    checker: ConnectivityChecker,
    corooutines: CoroutineContextProvider
) {
    val connectivityStatus: SharedFlow<Boolean> = connectivityCheckTimer
        .tick()
        .map { checker.check() }
        .shareIn(corooutines.ioScope, started = SharingStarted.WhileSubscribed(), replay = 1)
}