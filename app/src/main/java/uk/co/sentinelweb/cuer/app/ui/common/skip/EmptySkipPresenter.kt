package uk.co.sentinelweb.cuer.app.ui.common.skip

import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

class EmptySkipPresenter : SkipContract.External {
    override val skipForwardText: String = ""
    override val skipBackText: String = ""
    override val skipForwardInterval: Int = 0
    override val skipBackInterval: Int = 0
    override var duration: Long = 0L
    override lateinit var listener: SkipContract.Listener

    override fun skipFwd() = Unit

    override fun skipBack() = Unit

    override fun updatePosition(ms: Long) = Unit

    override fun stateChange(playState: PlayerStateDomain) = Unit

    override fun onSelectSkipTime(fwd: Boolean) = Unit

    override fun updateSkipTimes() = Unit
}