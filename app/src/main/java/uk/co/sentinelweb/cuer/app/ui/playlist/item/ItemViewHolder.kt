package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemViewHolder(
    val itemPresenter: ItemContract.External, private val view: ItemView
) : RecyclerView.ViewHolder(view),
    ItemContract.ItemTouchHelperViewHolder {

    // region ItemTouchHelperViewHolder
    override val contentView: View
        get() = view.itemView

    override val rightSwipeView: View
        get() = view.rightSwipeView

    override val leftSwipeView: View
        get() = view.leftSwipeView

    override fun onItemSwiped(left: Boolean) {
        if (left) {
            itemPresenter.doLeft()
        } else {
            itemPresenter.doRight()
        }
    }

    override fun onItemSelected() {
        // itemPresenter.doClick()
    }

    override fun onItemClear() {
        view.resetBackground()
    }
    // endregion
}