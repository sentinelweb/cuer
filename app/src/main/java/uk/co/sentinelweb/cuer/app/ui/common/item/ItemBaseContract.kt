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
        fun canDragLeft(): Boolean
        fun canDragRight(): Boolean
        fun canReorder(): Boolean
    }

    interface ItemPresenterBase {
        fun canDragLeft(): Boolean
        fun canDragRight(): Boolean
        fun canReorder(): Boolean
    }

    interface ItemMoveInteractions {
        fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
        fun onItemClear()
    }

}
