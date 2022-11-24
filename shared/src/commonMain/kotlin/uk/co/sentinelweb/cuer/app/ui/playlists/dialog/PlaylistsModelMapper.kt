package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Model
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Model.Header
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract.Companion.ROOT_PLAYLIST_DUMMY
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain
import uk.co.sentinelweb.cuer.domain.ext.iterate

// todo strings
class PlaylistsModelMapper constructor(
    // private val res: ResourceWrapper
) {

    fun map(
        channelPlaylists: List<PlaylistDomain>,
        recentPlaylists: List<PlaylistDomain>,
        current: OrchestratorContract.Identifier<*>?,
        pinnedId: Long?,
        tree: PlaylistTreeDomain,
        playlistStats: Map<Long?, PlaylistStatDomain?>,
        showRoot: Boolean,
    ): PlaylistsMviContract.View.Model {
        val items = mutableListOf<Model>()
        if (showRoot) {
            items.add(itemModel(ROOT_PLAYLIST_DUMMY, null, pinnedId, 0))
        }
        channelPlaylists
            .takeIf { it.isNotEmpty() }
            ?.let { list ->
                items.add(Header(-1L, "For this channel"))//res.getString(R.string.playlists_section_channel)
                items.addAll(channelPlaylists.map { itemModel(it, playlistStats[it.id], pinnedId, 0) })
            }

        recentPlaylists
            .takeIf { it.isNotEmpty() }
            ?.let { list ->
                items.add(Header(-2L, "Recent")) // res.getString(R.string.playlists_section_recent)
                items.addAll(list.map { itemModel(it, playlistStats[it.id], pinnedId, 0) })
            }
        items.add(Header(-3L, "All")) // res.getString(R.string.playlists_section_all)
        tree.iterate { treeNode, depth ->
            treeNode.node?.also {
                items.add(itemModel(it, playlistStats[it.id], pinnedId, depth - 1))
            }
        }
        return PlaylistsMviContract.View.Model(
            title = "Select playlist",//res.getString(R.string.playlists_title)
            currentPlaylistId = current,
            items = items
        )
    }

    private fun itemModel(
        pl: PlaylistDomain,
        stats: PlaylistStatDomain?,
        pinnedId: Long?,
        depth: Int
    ) = Model.Item(
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
