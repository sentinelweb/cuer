package uk.co.sentinelweb.cuer.app.ui.playlists.item

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
        fun doClick()
        fun doPlay(external: Boolean)
        fun doStar()
        fun doShare()
    }

    interface External {
        fun update(item: ItemModel)
        fun doLeft()
        fun doRight()
    }

    interface Interactions {
        fun onClick(item: ItemModel)
        fun onRightSwipe(item: ItemModel)
        fun onLeftSwipe(item: ItemModel)
        fun onPlay(item: ItemModel, external: Boolean)
        fun onStar(item: ItemModel)
        fun onShare(item: ItemModel)
    }

}
