package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModel
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface PlaylistContract {

    interface Presenter {
        fun initialise()
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
        fun scroll(direction: ScrollDirection)
        fun undoDelete()
        fun commitMove()
        fun setPlaylistData(plId: Long?, plItemId: Long?, playNow: Boolean)
        fun onPlaylistSelected(playlist: PlaylistDomain)
    }

    interface View {
        fun setList(list: List<ItemModel>, animate: Boolean = true)
        fun scrollToItem(index: Int)
        fun scrollTo(direction: ScrollDirection)
        fun playLocal(media: MediaDomain)
        fun showDeleteUndo(msg: String)
        fun highlightPlayingItem(currentItemIndex: Int?)
        fun setSubTitle(subtitle: String)
        fun showPlaylistSelector(model: SelectDialogModel)
        fun showPlaylistCreateDialog()
        fun showAlertDialog(model: AlertDialogModel)
        fun resetItemsState()
    }

    enum class ScrollDirection { Up, Down, Top, Bottom }
}