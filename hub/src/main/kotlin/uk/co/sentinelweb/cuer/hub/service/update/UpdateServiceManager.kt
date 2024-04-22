package uk.co.sentinelweb.cuer.hub.service.update

import uk.co.sentinelweb.cuer.app.service.update.UpdateServiceContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class UpdateServiceManager constructor(
    private val log: LogWrapper
) : UpdateServiceContract.Manager {
    init {
        log.tag(this)
    }

    override fun start() = log.d("start")

    override fun stop() = log.d("stop")
}