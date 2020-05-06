package uk.co.sentinelweb.cuer.app.ui.playlist.item

import androidx.annotation.DrawableRes

interface ItemContract {

    interface View {
        fun setTopText(text: String)
        fun setBottomText(text: String)
        fun setIconResource(@DrawableRes iconRes: Int)
        fun setCheckedVisible(checked: Boolean)
        fun setPresenter(itemPresenter: Presenter)
        fun setIconUrl(url: String)
    }

    interface Presenter {
        fun update(item: ItemModel)
        fun doClick()
        fun doLeft()
        fun doRight()
        fun doPlay(external: Boolean)
        fun doShowChannel()
        fun doStar()
        fun doShare()
    }

    interface Interactions {
        fun onClick(item: ItemModel)
        fun onRightSwipe(item: ItemModel)
        fun onLeftSwipe(item: ItemModel)
        fun onPlay(item: ItemModel, external: Boolean)
        fun onShowChannel(item: ItemModel)
        fun onStar(item: ItemModel)
        fun onShare(item: ItemModel)
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
    }

}
