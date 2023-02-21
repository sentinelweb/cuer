package uk.co.sentinelweb.cuer.remote.server

interface WakeLockManager {
    fun acquireWakeLock()
    fun releaseWakeLock()
}