package uk.co.sentinelweb.cuer.hub.ui.player.cast

import uk.co.sentinelweb.cuer.app.ui.cast.CastContract

class EmptyCastDialogLauncher : CastContract.CastDialogLauncher {
    override fun launchCastDialog() = Unit

    override fun hideCastDialog() = Unit
}