package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseModel
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.toGUID

class PlaylistsItemMviContract {

    // used to retain the view while passing through the MVI - Don't hold
    interface ItemPassView

    sealed class Model(override val id: Identifier<GUID>) : ItemBaseModel(id) {

        data class Header(
            override val id: Identifier<GUID>,
            val title: String,
        ) : Model(id)

        data class List(
            override val id: Identifier<GUID>,
            val items: kotlin.collections.List<Item>,
        ) : Model(id)

        data class Item(
            override val id: Identifier<GUID>,
            val title: String,
            val checkIcon: Boolean,
            val thumbNailUrl: String?,
            val starred: Boolean,
            val count: Int,
            val newItems: Int,
            val loopMode: PlaylistDomain.PlaylistModeDomain,
            val type: PlaylistDomain.PlaylistTypeDomain,
            val platform: PlatformDomain?,
            val showOverflow: Boolean,
            val source: OrchestratorContract.Source,
            val canPlay: Boolean,
            val canEdit: Boolean,
            val canDelete: Boolean,
            val canLaunch: Boolean,
            val canShare: Boolean,
            val watched: Boolean,
            val pinned: Boolean,
            val default: Boolean,
            val depth: Int
        ) : Model(id)
    }

    companion object {
        val ID_APP_HEADER = Identifier("id-app-header".toGUID(), MEMORY)
        val ID_APP_LIST = Identifier("id-app-list".toGUID(), MEMORY)
        val ID_RECENT_HEADER = Identifier("id-recent-header".toGUID(), MEMORY)
        val ID_RECENT_LIST = Identifier("id-recent-list".toGUID(), MEMORY)
        val ID_STARRED_HEADER = Identifier("id-starred-header".toGUID(), MEMORY)
        val ID_STARRED_LIST = Identifier("id-starred-list".toGUID(), MEMORY)
        val ID_ALL_HEADER = Identifier("id-all-header".toGUID(), MEMORY)
    }
}