package uk.co.sentinelweb.cuer.app.util.remote

import android.app.Application
import org.koin.core.component.KoinComponent
import uk.co.sentinelweb.cuer.app.ui.exoplayer.ExoActivity
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.remote.interact.PlayerLaunchHost

class AppPlayerLaunchHost(
    private val appContext: Application,
    private val log: LogWrapper
) : PlayerLaunchHost, KoinComponent {

    init {
        log.tag(this)
    }

    override fun launchVideo(item: PlaylistItemDomain, screenIndex: Int?) {
        log.d("launchVideo: item = ${item.media.title}")
        ExoActivity.start(appContext, PlaylistAndItemDomain(item, null, null))
    }
}
