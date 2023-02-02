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
        val ID_APP_HEADER = Identifier("5e9b262f-0ce8-4b95-a4f7-a9565d01732a".toGUID(), MEMORY)
        val ID_APP_LIST = Identifier("5c609f4a-d7d7-4124-bf09-65ab79dc0a21".toGUID(), MEMORY)
        val ID_RECENT_HEADER = Identifier("4b44b22f-0c3c-4767-b5ae-7b20b39f5dd2".toGUID(), MEMORY)
        val ID_RECENT_LIST = Identifier("956151dc-670a-4e46-b8b4-a72ef5a719ed".toGUID(), MEMORY)
        val ID_STARRED_HEADER = Identifier("50478d2b-6c4c-4e6f-8a18-403a4acc6520".toGUID(), MEMORY)
        val ID_STARRED_LIST = Identifier("50f44d8b-9261-4eab-9ddb-937603e90168".toGUID(), MEMORY)
        val ID_ALL_HEADER = Identifier("1b857a31-01ab-44ed-a05a-5704f13b1c2e".toGUID(), MEMORY)
    }
}