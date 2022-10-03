package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract.Companion.ID_ALL_HEADER
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract.Companion.ID_APP_HEADER
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract.Companion.ID_APP_LIST
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract.Companion.ID_RECENT_LIST
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract.Companion.ID_STARRED_LIST
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain
import uk.co.sentinelweb.cuer.domain.ext.iterate

class PlaylistsModelMapper constructor(
    private val res: ResourceWrapper
) {

    fun map(
        domains: Map<PlaylistDomain, PlaylistStatDomain?>,
        current: OrchestratorContract.Identifier<*>?,
        appPlaylists: Map<PlaylistDomain, PlaylistStatDomain?>,
        recentPlaylists: List<OrchestratorContract.Identifier<Long>>,
        pinnedId: Long?,
        root: PlaylistTreeDomain
    ): PlaylistsContract.Model {
        return PlaylistsContract.Model(
            res.getString(R.string.playlists_title),
            PLAYLISTS_HEADER_IMAGE,
            current,
            buildItems(domains, appPlaylists, recentPlaylists, pinnedId, root)
        )
    }

    private fun buildItems(
        domains: Map<PlaylistDomain, PlaylistStatDomain?>,
        appPlaylists: Map<PlaylistDomain, PlaylistStatDomain?>,
        recentPlaylists: List<OrchestratorContract.Identifier<Long>>,
        pinnedId: Long?,
        root: PlaylistTreeDomain
    ): List<ItemContract.Model> {
        val starred = buildStarredList(domains)
        val recent = buildRecentList(domains, recentPlaylists)
        val list = mutableListOf(

            ItemContract.Model.HeaderModel(
                ID_APP_HEADER,
                res.getString(R.string.playlists_section_app)
            ),
            ItemContract.Model.ListModel(
                ID_APP_LIST,
                appPlaylists.keys.map {
                    itemModel(it, it.id == pinnedId, appPlaylists[it], 0)
                }),

            ItemContract.Model.HeaderModel(
                ID_RECENT_LIST,
                res.getString(R.string.playlists_section_recent)
            ),
            ItemContract.Model.ListModel(ID_RECENT_LIST,
                recent.map {
                    itemModel(it, it.id == pinnedId, domains[it], 0)
                }),

            ItemContract.Model.HeaderModel(
                ID_STARRED_LIST,
                res.getString(R.string.playlists_section_starred)
            ),
            ItemContract.Model.ListModel(
                ID_STARRED_LIST,
                starred.map {
                    itemModel(it, it.id == pinnedId, domains[it], 0)
                }),

            ItemContract.Model.HeaderModel(
                ID_ALL_HEADER,
                res.getString(R.string.playlists_section_all)
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
        starred.find { it.default }
            ?: run { starred.add(0, domains.keys.find { it.default }!!) }
        return starred
    }

    private fun itemModel(
        pl: PlaylistDomain,
        pinned: Boolean,
        playlistStatDomain: PlaylistStatDomain?,
        depth: Int
    ) = ItemContract.Model.ItemModel(
        pl.id ?: throw Exception("Playlist must have an id"),
        pl.title,
        false,
        (pl.thumb ?: pl.image)?.url,
        count = playlistStatDomain?.itemCount ?: -1,
        newItems = playlistStatDomain?.let { it.itemCount - it.watchedItemCount } ?: -1,
        starred = pl.starred,
        loopMode = pl.mode,
        type = pl.type,
        platform = pl.platform,
        showOverflow = true,
        source = if (pl.type == APP) MEMORY else LOCAL,
        canEdit = pl.config.editable,
        canPlay = pl.config.playable,
        canDelete = pl.config.deletable,
        canLaunch = pl.type == PLATFORM,
        canShare = pl.type != APP,
        watched = playlistStatDomain?.let { it.watchedItemCount == it.itemCount } ?: false,
        pinned = pinned,
        default = pl.default,
        depth = depth
    )

    companion object {
        const val PLAYLISTS_HEADER_IMAGE =
            "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg"
    }

}
