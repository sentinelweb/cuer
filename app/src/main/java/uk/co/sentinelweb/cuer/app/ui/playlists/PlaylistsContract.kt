package uk.co.sentinelweb.cuer.app.ui.playlists

import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModel
import uk.co.sentinelweb.cuer.domain.MediaDomain

interface PlaylistsContract {

    interface Presenter {
        fun initialise()
        fun destroy()
        fun refreshList()
        fun setFocusMedia(mediaDomain: MediaDomain)
        fun onItemSwipeRight(item: ItemModel)
        fun onItemSwipeLeft(item: ItemModel)
        fun onItemClicked(item: ItemModel)
        fun onItemPlay(item: ItemModel, external: Boolean)
        fun onItemStar(item: ItemModel)
        fun onItemShare(item: ItemModel)
        fun moveItem(fromPosition: Int, toPosition: Int)

        //        fun scroll(direction: ScrollDirection)
        fun undoDelete()
        fun commitMove()
        fun onResume()
    }

    interface View {
        fun setList(list: List<ItemModel>, animate: Boolean = true)
        fun scrollToItem(index: Int)

        //fun scrollTo(direction: ScrollDirection)
        //fun playLocal(media: MediaDomain)
        fun showDeleteUndo(msg: String)
        fun gotoPlaylist(id: Long, play: Boolean)
        fun gotoEdit(id: Long)
    }

    enum class ScrollDirection { Up, Down, Top, Bottom }
}