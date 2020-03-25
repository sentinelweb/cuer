package uk.co.sentinelweb.cuer.app.util.cast.ui

import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.ConnectionState.CC_DISCONNECTED
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.PlayerStateUi

class CastPlayerState constructor(
    var connectionState: ConnectionState = CC_DISCONNECTED,
    val listeners: MutableList<CastPlayerContract.PresenterExternal.Listener> = mutableListOf(),
    var playState: PlayerStateUi = PlayerStateUi.UNKNOWN,
    var positionMs: Long = 0,
    var durationMs: Long = 0,
    var title: String = ""
) : ViewModel()
