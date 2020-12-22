package uk.co.sentinelweb.cuer.app.ui.playlist.item

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

interface ItemContract {

    interface View {
        fun setTopText(text: String)
        fun setBottomText(text: String)
        fun setIconResource(@DrawableRes iconRes: Int)
        fun setCheckedVisible(checked: Boolean)
        fun setPresenter(itemPresenter: Presenter)
        fun setIconUrl(url: String)
        fun setBackground(@ColorRes backgroundColor: Int)
    }

    interface Presenter {
        fun doClick()
        fun doPlay(external: Boolean)
        fun doShowChannel()
        fun doStar()
        fun doShare()
        fun doView()
        fun doPlayStartClick()
    }

    interface External {
        fun update(item: ItemModel, highlightPlaying: Boolean)
        fun doLeft()
        fun doRight()
    }

    interface Interactions {
        fun onClick(item: ItemModel)
        fun onRightSwipe(item: ItemModel)
        fun onLeftSwipe(item: ItemModel)
        fun onPlay(item: ItemModel, external: Boolean)
        fun onShowChannel(item: ItemModel)
        fun onStar(item: ItemModel)
        fun onShare(item: ItemModel)
        fun onView(item: ItemModel)
        fun onPlayStartClick(item: ItemModel)
    }

}
