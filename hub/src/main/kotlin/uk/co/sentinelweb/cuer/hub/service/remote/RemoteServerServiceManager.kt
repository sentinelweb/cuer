package uk.co.sentinelweb.cuer.hub.service.remote

import org.koin.java.KoinJavaComponent.getKoin
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class RemoteServerServiceManager(
    private val log: LogWrapper
) : RemoteServerContract.Manager {

    private var service: RemoteServerContract.Service? = null

    init {
        log.tag(this)
    }

    override fun start() {
        if (!isRunning()) {
            service = getKoin().get()
        }
    }

    override fun stop() {
        if (isRunning()) {
            service?.stopSelf()
            service = null
        }
    }

    override fun getService(): RemoteServerContract.Service? = null

    override fun isRunning(): Boolean = service != null


}