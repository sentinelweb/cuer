package uk.co.sentinelweb.cuer.app.util.cast.ui

import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract.PlayerStateUi

// todo duplicates notif state fields possibly state should only be in listener - think after done
data class CastPlayerState constructor(
    val listeners: MutableList<CastPlayerContract.PlayerControls.Listener> = mutableListOf(), // todo data only here move to presenter?
    var playState: PlayerStateUi = PlayerStateUi.UNKNOWN,
    var positionMs: Long = 0,
    var seekPositionMs: Long = 0,
    var durationMs: Long = 0,
    var title: String = "",
    var isDestroyed: Boolean = false
) : ViewModel()
