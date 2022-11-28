package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Companion.ID_ALL_HEADER
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Companion.ID_APP_HEADER
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Companion.ID_APP_LIST
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Companion.ID_RECENT_HEADER
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Companion.ID_RECENT_LIST
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Companion.ID_STARRED_HEADER
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Companion.ID_STARRED_LIST
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract.View
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain
import uk.co.sentinelweb.cuer.domain.ext.iterate

class PlaylistsMviModelMapper(
    private val strings: PlaylistsMviContract.Strings
) {

    fun map(state: MviStore.State): View.Model = View.Model(
        //playlists.associateWith { pl -> playlistStats.find { it.playlistId == pl.id } },
        currentPlaylistId = state.currentPlayingPlaylistId,
        items = buildItems(
            domains = state.playlists.associateWith { pl -> state.playlistStats.find { it.playlistId == pl.id } },
            appPlaylists = state.appLists,
            recentPlaylists = state.recentPlaylists,
            pinnedId = state.pinnedPlaylistId,
            root = state.treeRoot
        ),
        title = "Playlists"
    )

    private fun buildItems(
        domains: Map<PlaylistDomain, PlaylistStatDomain?>,
        appPlaylists: Map<PlaylistDomain, PlaylistStatDomain?>,
        recentPlaylists: List<OrchestratorContract.Identifier<Long>>,
        pinnedId: Long?,
        root: PlaylistTreeDomain
    ): List<PlaylistsItemMviContract.Model> {
        val starred = buildStarredList(domains)
        val recent = buildRecentList(domains, recentPlaylists)
        val list = mutableListOf(

            PlaylistsItemMviContract.Model.Header(
                ID_APP_HEADER,
                strings.playlists_section_app
            ),
            PlaylistsItemMviContract.Model.List(
                ID_APP_LIST,
                appPlaylists.keys.map {
                    itemModel(it, it.id == pinnedId, appPlaylists[it], 0)
                }),

            PlaylistsItemMviContract.Model.Header(
                ID_RECENT_HEADER,
                strings.playlists_section_recent
            ),
            PlaylistsItemMviContract.Model.List(ID_RECENT_LIST,
                recent.map {
                    itemModel(it, it.id == pinnedId, domains[it], 0)
                }),

            PlaylistsItemMviContract.Model.Header(
                ID_STARRED_HEADER,
                strings.playlists_section_starred
            ),
            PlaylistsItemMviContract.Model.List(
                ID_STARRED_LIST,
                starred.map {
                    itemModel(it, it.id == pinnedId, domains[it], 0)
                }),

            PlaylistsItemMviContract.Model.Header(
                ID_ALL_HEADER,
                strings.playlists_section_all
            ),
        )
        root.iterate { tree, depth ->
            tree.node?.also {
                list.add(itemModel(it, it.id == pinnedId, domains[it], depth - 1))
            }
        }
        return list
    }

    private fun buildRecentList(
        domains: Map<PlaylistDomain, PlaylistStatDomain?>,
        recentPlaylists: List<OrchestratorContract.Identifier<Long>>,
    ): List<PlaylistDomain> =
        recentPlaylists
            .mapNotNull { identifier -> domains.keys.find { it.id == identifier.id } }

    private fun buildStarredList(
        domains: Map<PlaylistDomain, PlaylistStatDomain?>
    ): List<PlaylistDomain> {
        val starred = domains.keys.filter { it.starred }
            .sortedBy { it.title.lowercase() }
            .toMutableList()
        starred
            .find { it.default }
            ?: run {
                domains.keys
                    .takeIf { it.size > 0 }
                    ?.apply {
                        starred.add(
                            find { it.default }
                                ?: throw IllegalStateException("No default playlist")
                        )
                    }

            }
        return starred
    }

    private fun itemModel(
        pl: PlaylistDomain,
        pinned: Boolean,
        playlistStatDomain: PlaylistStatDomain?,
        depth: Int
    ) = PlaylistsItemMviContract.Model.Item(
        pl.id ?: throw Exception("Playlist must have an id"),
        pl.title.capitalize(),
        false,
        (pl.thumb ?: pl.image)?.url,
        count = playlistStatDomain?.itemCount ?: -1,
        newItems = playlistStatDomain?.let { it.itemCount - it.watchedItemCount } ?: -1,
        starred = pl.starred,
        loopMode = pl.mode,
        type = pl.type,
        platform = pl.platform,
        showOverflow = true,
        source = if (pl.type == PlaylistDomain.PlaylistTypeDomain.APP) MEMORY else LOCAL,
        canEdit = pl.config.editable,
        canPlay = pl.config.playable,
        canDelete = pl.config.deletable,
        canLaunch = pl.type == PlaylistDomain.PlaylistTypeDomain.PLATFORM,
        canShare = pl.type != PlaylistDomain.PlaylistTypeDomain.APP,
        watched = playlistStatDomain?.let { it.watchedItemCount == it.itemCount } ?: false,
        pinned = pinned,
        default = pl.default,
        depth = depth
    )
}