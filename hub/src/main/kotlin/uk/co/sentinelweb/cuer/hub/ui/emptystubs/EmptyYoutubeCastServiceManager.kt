package uk.co.sentinelweb.cuer.hub.ui.emptystubs

import uk.co.sentinelweb.cuer.app.service.cast.CastServiceContract

class EmptyYoutubeCastServiceManager : CastServiceContract.Manager {
    override fun start() = Unit

    override fun stop() = Unit

    override fun get(): CastServiceContract.Service? = null

    override fun isRunning(): Boolean = false
}
