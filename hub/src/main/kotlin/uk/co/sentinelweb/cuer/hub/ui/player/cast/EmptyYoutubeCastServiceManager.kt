package uk.co.sentinelweb.cuer.hub.ui.player.cast

import uk.co.sentinelweb.cuer.app.service.cast.YoutubeCastServiceContract

class EmptyYoutubeCastServiceManager : YoutubeCastServiceContract.Manager {
    override fun start() = Unit

    override fun stop() = Unit

    override fun get(): YoutubeCastServiceContract.Service? = null

    override fun isRunning(): Boolean = false
}