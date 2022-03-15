package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import android.util.Log
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain
import uk.co.sentinelweb.cuer.domain.ext.descendents
import uk.co.sentinelweb.cuer.domain.ext.iterate

class PlaylistsModelMapper constructor(
    private val res: ResourceWrapper
) {

    fun map(
        priorityPlaylists: List<PlaylistDomain>,
        current: OrchestratorContract.Identifier<*>?,
        pinnedId: Long?,
        tree: PlaylistTreeDomain,
        playlistStats: Map<Long?, PlaylistStatDomain?>,
        ): PlaylistsContract.Model {
        val items = priorityPlaylists.mapIndexed { index, pl ->
            itemModel(pl, playlistStats[pl.id], pinnedId, 0)
        }.toMutableList()
        tree.iterate { treeNode, depth ->
            treeNode.node?.also {
                items.add(itemModel(it, playlistStats[it.id], pinnedId, depth - 1))
            }
        }
        return PlaylistsContract.Model(
            title = res.getString(R.string.playlists_title),
            currentPlaylistId = current,
            items = items
        )
    }

    private fun itemModel(
        pl: PlaylistDomain,
        stats:PlaylistStatDomain?,
        pinnedId: Long?,
        depth: Int
    ) = ItemContract.Model.ItemModel(
        pl.id ?: -1L,
        pl.title,
        false,
        (pl.thumb ?: pl.image)?.url,
        count = stats?.itemCount ?: -1,
        newItems = stats?.let { it.itemCount - it.watchedItemCount } ?: -1,
        starred = pl.starred,
        loopMode = pl.mode,
        type = pl.type,
        platform = pl.platform,
        showOverflow = false,
        source = if (pl.type == APP) MEMORY else LOCAL,
        canEdit = false,
        canPlay = false,
        canDelete = false,
        canLaunch = pl.type == PLATFORM,
        canShare = pl.type != APP,
        watched = stats?.let { it.watchedItemCount == it.itemCount } ?: false,
        pinned = pl.id == pinnedId,
        default = pl.default,
        depth = depth
    )

}