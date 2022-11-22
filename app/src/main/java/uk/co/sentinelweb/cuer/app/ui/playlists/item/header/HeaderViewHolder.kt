package uk.co.sentinelweb.cuer.app.ui.playlists.item.header

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract

class HeaderViewHolder(
    private val headerView: ItemContract.HeaderView
) : RecyclerView.ViewHolder(headerView.root),
    ItemBaseContract.ItemTouchHelperViewHolder {

    override val contentView: View
        get() = headerView.root

    override val rightSwipeView: View?
        get() = null

    override val leftSwipeView: View?
        get() = null

    fun update(headerModel: PlaylistsItemMviContract.Model.HeaderModel) {
        headerView.setTitle(headerModel.title)
    }

    override fun onItemSwiped(left: Boolean) {}

    override fun canDragLeft(): Boolean = false

    override fun canDragRight(): Boolean = false

    override fun canReorder(): Boolean = false//itemPresenter.canReorder()

    override fun onItemClear() {}

    override fun onItemSelected() {}


    // endregion
}