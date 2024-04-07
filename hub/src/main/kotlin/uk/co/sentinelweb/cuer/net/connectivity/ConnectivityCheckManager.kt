package uk.co.sentinelweb.cuer.net.connectivity

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

// not used - just for testing
class ConnectivityCheckManager(
    private val coroutines: CoroutineContextProvider,
    private val monitor: ConnectivityMonitor,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    private var checkJob: Job? = null

    fun start() {
        coroutines.mainScope.launch {
            monitor.connectivityStatus.collect { status ->
                println(if (status) "Connected" else "Disconnected")
            }
        }
    }

    fun stop() {
        checkJob?.cancel()
        checkJob = null
    }
}
