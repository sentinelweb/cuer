package uk.co.sentinelweb.cuer.app.ui.ytplayer

import android.app.Activity
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_AND_ITEM
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistAndItem

class ItemLoader(
    private val activity: Activity,
    private val log: LogWrapper
) : PlayerContract.PlaylistItemLoader {

    override fun load(): PlaylistAndItemDomain? =
        activity.intent
            ?.getStringExtra(PLAYLIST_AND_ITEM.toString())
            //?.apply {log.d("item: "+this)}
            ?.let { deserialisePlaylistAndItem(it) }
}