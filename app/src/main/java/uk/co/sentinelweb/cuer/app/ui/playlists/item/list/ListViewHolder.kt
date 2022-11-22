package uk.co.sentinelweb.cuer.app.ui.playlists.item.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract.Model
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract

class ListViewHolder(
    private val listView: ListView,
    val listPresenter: ItemContract.External<Model.ListModel>
) : RecyclerView.ViewHolder(listView.root), ItemBaseContract.ItemTouchHelperViewHolder {

    override val contentView: View
        get() = listView.root

    override val rightSwipeView: View?
        get() = null

    override val leftSwipeView: View?
        get() = null

    override fun onItemSwiped(left: Boolean) {}

    override fun canDragLeft(): Boolean = listPresenter.canDragLeft()

    override fun canDragRight(): Boolean = listPresenter.canDragRight()

    override fun canReorder(): Boolean = listPresenter.canReorder()

    override fun onItemClear() {}

    override fun onItemSelected() {}
}