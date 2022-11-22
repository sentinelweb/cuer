package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseModel
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistsItemMviContract {

    // used to retain the view while passing through the MVI - Don't hold
    interface ItemPassView

    sealed class Model(override val id: Long) : ItemBaseModel(id) {

        data class HeaderModel(
            override val id: Long,
            val title: String,
        ) : Model(id)

        data class ListModel(
            override val id: Long,
            val items: List<ItemModel>,
        ) : Model(id)

        data class ItemModel(
            override val id: Long,// todo OrchestratorContract.Identifier
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
        const val ID_APP_HEADER = -1001L
        const val ID_APP_LIST = -1002L
        const val ID_RECENT_HEADER = -1003L
        const val ID_RECENT_LIST = -1004L
        const val ID_STARRED_HEADER = -1005L
        const val ID_STARRED_LIST = -1006L
        const val ID_ALL_HEADER = -1007L
    }
}