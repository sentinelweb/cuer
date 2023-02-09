package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseModel
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.common.resources.Color
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain

class PlaylistItemMviContract {

    // used to retain the view while passing through the MVI - Don't hold
    interface ItemPassView

    sealed class Model(override val id: OrchestratorContract.Identifier<GUID>) : ItemBaseModel(id) {

//        data class Header(
//            override val id: Long,
//            val title: String,
//        ) : Model(id)
//
//        data class List(
//            override val id: Long,
//            val items: kotlin.collections.List<Item>,
//        ) : Model(id)

        data class Item(
            override val id: OrchestratorContract.Identifier<GUID>,
            val index: Int,
            val url: String,
            val type: MediaDomain.MediaTypeDomain,
            val title: String,
            val duration: String,
            val positon: String,
            val imageUrl: String?,
            val thumbUrl: String?,
            val channelImageUrl: String?,
            val progress: Float, // 0..1
            val published: String,
            val watchedSince: String,
            val isWatched: Boolean,
            val isStarred: Boolean,
            val platform: PlatformDomain,
            val isLive: Boolean,
            val isUpcoming: Boolean,
            /*@ColorRes */val infoTextBackgroundColor: Color,
            val canEdit: Boolean,
            val playlistName: String?,
            val canDelete: Boolean,
            val canReorder: Boolean,
            val showOverflow: Boolean,
            val deleteResources: ActionResources?
        ) : Model(id)
    }

    companion object {

    }
}