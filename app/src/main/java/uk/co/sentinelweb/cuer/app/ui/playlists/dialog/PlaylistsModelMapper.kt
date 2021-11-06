package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

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

class PlaylistsModelMapper constructor(
    private val res: ResourceWrapper
) {

    fun map(
        domains: Map<PlaylistDomain, PlaylistStatDomain?>,
        current: OrchestratorContract.Identifier<*>?,
        showOverflow: Boolean,
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
            domains.keys.mapIndexed { index, pl ->
                ItemContract.Model.ItemModel(
                    pl.id ?: throw Exception("Playlist must have an id"),
                    pl.title,
                    false,
                    (pl.thumb ?: pl.image)?.url,
                    count = domains[pl]?.itemCount ?: -1,
                    newItems = domains[pl]?.let { it.itemCount - it.watchedItemCount } ?: -1,
                    starred = pl.starred,
                    loopMode = pl.mode,
                    type = pl.type,
                    platform = pl.platform,
                    showOverflow = showOverflow,
                    source = if (pl.type == APP) MEMORY else LOCAL,
                    canEdit = pl.config.editable,
                    canPlay = pl.config.playable,
                    canDelete = pl.config.deletable,
                    canLaunch = pl.type == PLATFORM,
                    canShare = pl.type != APP,
                    watched = domains[pl]?.let { it.watchedItemCount == it.itemCount } ?: false,
                    pinned = pl.id == pinnedId,
                    default = pl.default,
                    depth =0
                )
            }
        )
    }

    companion object {
        const val PLAYLISTS_HEADER_IMAGE = "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg"
    }

}
