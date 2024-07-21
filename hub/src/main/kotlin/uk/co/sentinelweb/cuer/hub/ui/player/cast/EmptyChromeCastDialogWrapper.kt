package uk.co.sentinelweb.cuer.hub.ui.player.cast

import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromeCastContract.DialogWrapper

class EmptyChromeCastDialogWrapper : DialogWrapper {
    override fun showRouteSelector() = Unit
}