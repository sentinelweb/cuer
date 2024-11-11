package uk.co.sentinelweb.cuer.app.util.player

import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_AND_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.EXO_PLAYER_FULL
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.remote.interact.PlayerLaunchHost

class AndroidPlayerLaunchHost(
    private val router: NavigationRouter
): PlayerLaunchHost {
    override fun launchPlayerVideo(item: PlaylistItemDomain, screenIndex: Int?) {
        router.navigate(
            NavigationModel(
                target = EXO_PLAYER_FULL,
                params = mapOf(PLAYLIST_AND_ITEM to PlaylistAndItemDomain(item, null, null))
            )
        )
    }
}
