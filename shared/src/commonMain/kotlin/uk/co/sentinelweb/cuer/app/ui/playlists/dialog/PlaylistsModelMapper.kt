package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Model
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Model.Header
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract.Companion.ROOT_PLAYLIST_DUMMY
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.ext.iterate

class PlaylistsModelMapper constructor(
    private val strings: PlaylistsMviDialogContract.Strings
) {

    fun map(
        channelPlaylists: List<PlaylistDomain>,
        recentPlaylists: List<PlaylistDomain>,
        current: Identifier<GUID>?,
        pinnedId: GUID?,
        tree: PlaylistTreeDomain,
        playlistStats: Map<Identifier<GUID>?, PlaylistStatDomain?>,
        showRoot: Boolean,
    ): PlaylistsMviContract.View.Model {
        val items = mutableListOf<Model>()
        if (showRoot) {
            items.add(itemModel(ROOT_PLAYLIST_DUMMY, null, pinnedId, 0))
        }
        channelPlaylists
            .takeIf { it.isNotEmpty() }
            ?.let { list ->
                items.add(Header(ID_CHANNEL_HEADER, strings.playlists_section_channel))
                items.addAll(channelPlaylists.map { itemModel(it, playlistStats[it.id], pinnedId, 0) })
            }

        recentPlaylists
            .takeIf { it.isNotEmpty() }
            ?.let { list ->
                items.add(Header(ID_RECENT_HEADER, strings.playlists_section_recent))
                items.addAll(list.map { itemModel(it, playlistStats[it.id], pinnedId, 0) })
            }
        items.add(Header(ID_ALL_HEADER, strings.playlists_section_all))
        tree.iterate { treeNode, depth ->
            treeNode.node?.also {
                items.add(itemModel(it, playlistStats[it.id], pinnedId, depth - 1))
            }
        }
        return PlaylistsMviContract.View.Model(
            title = strings.playlists_dialog_title,
            currentPlaylistId = current,
            items = items
        )
    }

    private fun itemModel(
        pl: PlaylistDomain,
        stats: PlaylistStatDomain?,
        pinnedId: GUID?,
        depth: Int
    ) = Model.Item(
        pl.id ?: throw IllegalStateException("Playlist must have an id"),
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
        pinned = pinnedId?.let { pl.id?.id == it } ?: false,
        default = pl.default,
        depth = depth
    )

    companion object {
        val ID_CHANNEL_HEADER = Identifier("157bd426-2508-4d08-8c55-3aa23905f85c".toGUID(), MEMORY)
        val ID_RECENT_HEADER = Identifier("4b44b22f-0c3c-4767-b5ae-7b20b39f5dd2".toGUID(), MEMORY)
        val ID_ALL_HEADER = Identifier("1b857a31-01ab-44ed-a05a-5704f13b1c2e".toGUID(), MEMORY)
    }

}
