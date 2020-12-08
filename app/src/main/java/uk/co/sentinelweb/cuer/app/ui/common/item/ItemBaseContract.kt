package uk.co.sentinelweb.cuer.app.ui.common.item

interface ItemBaseContract {

    interface Interactions {

    }

    interface ItemTouchHelperViewHolder {
        val contentView: android.view.View
        val rightSwipeView: android.view.View
        val leftSwipeView: android.view.View
        fun onItemSelected()
        fun onItemClear()
        fun onItemSwiped(left: Boolean)
    }

    interface ItemMoveInteractions {
        fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
        fun onItemClear()
    }

}
