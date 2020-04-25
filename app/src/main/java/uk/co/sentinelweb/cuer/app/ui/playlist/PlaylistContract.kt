package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemModel

interface PlaylistContract {

    interface Presenter {
        fun initialise()
        fun loadList()
        fun destroy()
        fun onItemSwipeRight(item: PlaylistModel.PlaylistItemModel)
        fun onItemSwipeLeft(item: PlaylistModel.PlaylistItemModel)
        fun onItemClicked(item: PlaylistModel.PlaylistItemModel)
        fun refreshList()
    }

    interface View {
        fun setList(list: List<ItemModel>)
        fun showAlert(msg: String)
    }
}