package uk.co.sentinelweb.klink.ui.common.itemlist.item

import androidx.annotation.DrawableRes

interface ItemContract {
    interface View {
        fun setTopText(text:String)
        fun setBottomText(text:String)
        fun setIconResource(@DrawableRes iconRes:Int);
        fun setCheckedVisible(checked:Boolean)
        fun setPresenter(itemPresenter: Presenter)
    }

    interface Presenter {
        fun update(item:ItemModel)
        fun doClick()
        fun doLeft()
        fun doRight()
    }

    interface Interactions {
        fun onClick(item:ItemModel)
        fun onRightSwipe(item:ItemModel)
        fun onLeftSwipe(item:ItemModel)
    }

}
