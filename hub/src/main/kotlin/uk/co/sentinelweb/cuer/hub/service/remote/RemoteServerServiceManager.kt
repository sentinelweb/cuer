package uk.co.sentinelweb.cuer.hub.service.remote

import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class RemoteServerServiceManager(
    private val log: LogWrapper
) : RemoteServerContract.Manager {

    // private val service
    init {
        log.tag(this)
    }

    private var isRunning = false
    override fun start() {
        if (!isRunning()) {
            log.d("fake remote service started")
        }
    }

    override fun stop() {
        if (isRunning()) {
            log.d("fake remote service stopped")
        }
    }

    override fun getService(): RemoteServerContract.Service? = null

    override fun isRunning(): Boolean = isRunning


}