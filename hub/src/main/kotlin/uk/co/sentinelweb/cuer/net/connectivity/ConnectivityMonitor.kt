package uk.co.sentinelweb.cuer.net.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider

class ConnectivityMonitor(
    connectivityCheckTimer: ConnectivityCheckTimer,
    checker: ConnectivityChecker,
    coroutines: CoroutineContextProvider
) {
    val connectivityStatus: Flow<Boolean> = connectivityCheckTimer
        .tick()
        .map { checker.check() }
        .shareIn(coroutines.ioScope, started = SharingStarted.WhileSubscribed(), replay = 1)
}