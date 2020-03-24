package uk.co.sentinelweb.cuer.app.util.cast.ui

import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState

class CastPlayerState constructor(
    var connectionState: ConnectionState = ConnectionState.CC_DISCONNECTED
) : ViewModel()

