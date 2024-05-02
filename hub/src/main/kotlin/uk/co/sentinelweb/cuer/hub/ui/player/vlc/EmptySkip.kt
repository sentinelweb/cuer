package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

// todo move app skip presenter to shared - remove this
class EmptySkip : SkipContract.External {
    override val skipForwardText = "SkipFwd"
    override val skipBackText = "SkipBack"
    override val skipForwardInterval = 10 * 1000
    override val skipBackInterval = 10 * 1000
    override var duration: Long = 0

    override lateinit var listener: SkipContract.Listener

    override fun skipFwd() = Unit

    override fun skipBack() = Unit

    override fun updatePosition(ms: Long) = Unit

    override fun stateChange(playState: PlayerStateDomain) = Unit

    override fun onSelectSkipTime(fwd: Boolean) = Unit

    override fun updateSkipTimes() = Unit
}