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
        fun onPlayModeChange(): Boolean
        fun onPlayPlaylist(): Boolean
        fun onStarPlaylist(): Boolean
        fun onFilterNewItems(): Boolean
        fun onEdit(): Boolean
        fun onFilterPlaylistItems(): Boolean
        fun onResume()
        fun onPause()
    }

    interface View {
        fun setModel(model: PlaylistModel, animate: Boolean = true)
        fun setHeaderModel(model: PlaylistModel)
        fun setList(items: List<ItemContract.PlaylistItemModel>, animate: Boolean)
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
        fun gotoEdit(id: Long)
        fun showCastRouteSelectorDialog()
        fun setPlayState(state: PlayState)
    }

    enum class ScrollDirection { Up, Down, Top, Bottom }
    enum class PlayState { PLAYING, CONNECTING, NOT_CONNECTED }
}