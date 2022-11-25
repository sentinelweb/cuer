package uk.co.sentinelweb.cuer.app.ui.playlists.item.row

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract

class ItemRowViewHolder(
    val itemPresenter: ItemContract.External<PlaylistsItemMviContract.Model.Item>,
    private val rowView: ItemRowView
) : RecyclerView.ViewHolder(rowView.root),
    ItemBaseContract.ItemTouchHelperViewHolder {

    override val contentView: View
        get() = rowView.itemView

    override val rightSwipeView: View
        get() = rowView.rightSwipeView

    override val leftSwipeView: View
        get() = rowView.leftSwipeView

    override fun onItemSwiped(left: Boolean) {
        if (left) {
            itemPresenter.doLeft()
        } else {
            itemPresenter.doRight()
        }
    }

    override fun canDragLeft(): Boolean = itemPresenter.canDragLeft()

    override fun canDragRight(): Boolean = itemPresenter.canDragRight()

    override fun canReorder(): Boolean = itemPresenter.canReorder()

    override fun onItemSelected() {}

    override fun onItemClear() {
        rowView.resetBackground()
    }
    // endregion
}