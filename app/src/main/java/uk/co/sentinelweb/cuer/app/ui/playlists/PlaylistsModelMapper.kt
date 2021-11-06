package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain
import uk.co.sentinelweb.cuer.domain.ext.descendents

class PlaylistsModelMapper constructor(
    private val res: ResourceWrapper
) {

    fun map2(
        domains: Map<PlaylistDomain, PlaylistStatDomain?>,
        current: OrchestratorContract.Identifier<*>?,
        appPlaylists: Map<PlaylistDomain, PlaylistStatDomain?>,
        pinnedId: Long?,
        treeLookup: Map<Long, PlaylistTreeDomain>
    ): PlaylistsContract.Model {
        return PlaylistsContract.Model(
            res.getString(R.string.playlists_title),
            PLAYLISTS_HEADER_IMAGE,
            current,
            false,// show up
            buildItems(domains, current, appPlaylists, pinnedId, treeLookup)
        )
    }

    private fun buildItems(
        domains: Map<PlaylistDomain, PlaylistStatDomain?>,
        current: OrchestratorContract.Identifier<*>?,
        appPlaylists: Map<PlaylistDomain, PlaylistStatDomain?>,
        pinnedId: Long?,
        treeLookup: Map<Long, PlaylistTreeDomain>
    ): List<ItemContract.Model> {
        val starred = domains.keys.filter { it.starred }
            .sortedBy { it.title.lowercase() }
            .toMutableList()
        (starred.find { it.id == pinnedId }
            ?.also { starred.remove(it) }
            ?: let { domains.keys.find { it.id == pinnedId } })
            ?.also { starred.add(0, it) }
        // add default if not in list
        starred.find { it.default }
            ?: run { starred.add(0, domains.keys.find { it.default }!!) }
        // add current at start
        starred.removeIf { it.id == current?.id }
        domains.keys.find { it.id == current?.id }
            ?.also { starred.add(0, it) }
        return listOf(
            ItemContract.Model.HeaderModel(0, res.getString(R.string.playlists_section_app)),
            ItemContract.Model.ListModel(0, appPlaylists.keys.map {
                itemModel(it, treeLookup, it.id == pinnedId, appPlaylists[it])
            }),
            ItemContract.Model.HeaderModel(0, res.getString(R.string.playlists_section_starred)),
            ItemContract.Model.ListModel(0, starred.map {
                itemModel(it, treeLookup, it.id == pinnedId, domains[it])
            }),
            ItemContract.Model.HeaderModel(0, res.getString(R.string.playlists_section_all)),
        ).plus(domains.keys.map { pl ->
            itemModel(pl, treeLookup, pl.id == pinnedId, domains[pl])
        })
    }

    fun map(
        domains: Map<PlaylistDomain, PlaylistStatDomain?>,
        current: OrchestratorContract.Identifier<*>?,
        pinnedId: Long?,
        nodeId: Long?,
        treeLookup: Map<Long, PlaylistTreeDomain>
    ): PlaylistsContract.Model {
        return PlaylistsContract.Model(
            treeLookup[nodeId]?.node?.title?.let { it + ": " + res.getString(R.string.playlists_title) }
                ?: res.getString(R.string.playlists_title),
            treeLookup[nodeId]?.node?.image?.url ?: PLAYLISTS_HEADER_IMAGE,
            current,
            nodeId != null,
            domains.keys.map { pl ->
                itemModel(pl, treeLookup, pl.id == pinnedId, domains[pl])
            }
        )
    }

    private fun itemModel(
        pl: PlaylistDomain,
        treeLookup: Map<Long, PlaylistTreeDomain>,
        pinned: Boolean,
        playlistStatDomain: PlaylistStatDomain?
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
        descendents = treeLookup.get(pl.id)?.descendents() ?: 0
    )

    companion object {
        const val PLAYLISTS_HEADER_IMAGE =
            "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg"
    }

}
