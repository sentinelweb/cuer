package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.text.SpannableString
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseModel
import uk.co.sentinelweb.cuer.domain.MediaDomain

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
        fun update(item: PlaylistItemModel, highlightPlaying: Boolean)
        fun doLeft()
        fun doRight()
    }

    interface Interactions {
        fun onClick(item: PlaylistItemModel)
        fun onRightSwipe(item: PlaylistItemModel)
        fun onLeftSwipe(item: PlaylistItemModel)
        fun onPlay(item: PlaylistItemModel, external: Boolean)
        fun onShowChannel(item: PlaylistItemModel)
        fun onStar(item: PlaylistItemModel)
        fun onShare(item: PlaylistItemModel)
        fun onView(item: PlaylistItemModel)
        fun onPlayStartClick(item: PlaylistItemModel)
    }

    data class PlaylistItemModel constructor(
        override val id: Long,
        val index: Int,
        val url: String,
        val type: MediaDomain.MediaTypeDomain,
        val title: String,
        val length: String,
        val positon: String,
        val thumbNailUrl: String?,
        val bottomText: SpannableString,
        val progress: Float
    ) : ItemBaseModel(id)
}
