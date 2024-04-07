package uk.co.sentinelweb.cuer.hub.service.remote

import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain

class RemoteServerService(
    private val log: LogWrapper
) : RemoteServerContract.Service {

    init {
        log.tag(this)
    }

    override val isServerStarted: Boolean
        get() = TODO("Not yet implemented")
    override val localNode: LocalNodeDomain
        get() = TODO("Not yet implemented")
    override var stopListener: (() -> Unit)?
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun stopSelf() {
        log.d("Stop Self")
    }

    override suspend fun multicastPing() {
        TODO("Not yet implemented")
    }
}
