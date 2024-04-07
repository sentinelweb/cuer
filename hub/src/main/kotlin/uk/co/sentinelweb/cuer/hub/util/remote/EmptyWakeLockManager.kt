package uk.co.sentinelweb.cuer.hub.util.remote

import uk.co.sentinelweb.cuer.remote.server.WakeLockManager

class EmptyWakeLockManager : WakeLockManager {
    override fun acquireWakeLock() = Unit

    override fun releaseWakeLock() = Unit
}