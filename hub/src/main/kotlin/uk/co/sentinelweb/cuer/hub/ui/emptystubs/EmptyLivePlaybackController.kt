package uk.co.sentinelweb.cuer.hub.ui.emptystubs

import uk.co.sentinelweb.cuer.app.util.android_yt_player.live.LivePlaybackContract

class EmptyLivePlaybackController : LivePlaybackContract.Controller {
    override fun setCurrentPosition(ms: Long) = Unit

    override fun gotDuration(durationMs: Long) = Unit

    override fun gotVideoId(id: String) = Unit

    override fun getLiveOffsetMs(): Long = 0L

    override fun clear(id: String) = Unit
}
