package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface PlaylistContract {

    interface Presenter {
        fun initialise()
        fun destroy()
        fun refreshList()
        fun setFocusMedia(mediaDomain: MediaDomain)
        fun onItemSwipeRight(item: ItemContract.PlaylistItemModel)
        fun onItemSwipeLeft(item: ItemContract.PlaylistItemModel)
        fun onItemClicked(item: ItemContract.PlaylistItemModel)
        fun onItemPlay(item: ItemContract.PlaylistItemModel, external: Boolean)
        fun onItemShowChannel(item: ItemContract.PlaylistItemModel)
        fun onItemStar(item: ItemContract.PlaylistItemModel)
        fun onItemShare(item: ItemContract.PlaylistItemModel)
        fun onPlayStartClick(item: ItemContract.PlaylistItemModel)
        fun onItemViewClick(item: ItemContract.PlaylistItemModel)
        fun moveItem(fromPosition: Int, toPosition: Int)
        fun scroll(direction: ScrollDirection)
        fun undoDelete()
        fun commitMove()
        fun setPlaylistData(plId: Long?, plItemId: Long?, playNow: Boolean)
        fun onPlaylistSelected(playlist: PlaylistDomain)
    }

    interface View {
        fun setModel(model: PlaylistModel, animate: Boolean = true)
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
        fun showItemDescription(itemWitId: PlaylistItemDomain)
    }

    enum class ScrollDirection { Up, Down, Top, Bottom }
}