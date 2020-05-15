package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModel
import uk.co.sentinelweb.cuer.domain.MediaDomain

interface PlaylistContract {

    interface Presenter {
        fun initialise()
        fun loadList()
        fun destroy()
        fun refreshList()
        fun setFocusMedia(mediaDomain: MediaDomain)
        fun onItemSwipeRight(item: PlaylistModel.PlaylistItemModel)
        fun onItemSwipeLeft(item: PlaylistModel.PlaylistItemModel)
        fun onItemClicked(item: PlaylistModel.PlaylistItemModel)
        fun onItemPlay(item: PlaylistModel.PlaylistItemModel, external: Boolean)
        fun onItemShowChannel(item: PlaylistModel.PlaylistItemModel)
        fun onItemStar(item: PlaylistModel.PlaylistItemModel)
        fun onItemShare(item: PlaylistModel.PlaylistItemModel)
        fun moveItem(fromPosition: Int, toPosition: Int)
        fun playNow(mediaDomain: MediaDomain)
    }

    interface View {
        fun setList(list: List<ItemModel>)
        fun showAlert(msg: String)
        fun scrollToItem(index: Int)
        fun playLocal(media: MediaDomain)
    }
}