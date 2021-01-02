package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.text.SpannableString
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseModel
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain

interface ItemContract {

    interface View {
        fun setTopText(text: SpannableString)
        fun setBottomText(text: SpannableString)
        fun setIconResource(@DrawableRes iconRes: Int)
        fun setCheckedVisible(checked: Boolean)
        fun setPresenter(itemPresenter: Presenter)
        fun setIconUrl(url: String)
        fun setBackground(@ColorRes backgroundColor: Int)
        fun setDuration(text: String)
        fun setProgress(ratio: Float)
    }

    interface Presenter {
        fun doClick()
        fun doPlay(external: Boolean)
        fun doShowChannel()
        fun doStar()
        fun doShare()
        fun doView()
        fun doPlayStartClick()
        fun updateProgress()
    }

    interface External {
        fun update(item: Model, highlightPlaying: Boolean)
        fun doLeft()
        fun doRight()
    }

    interface Interactions {
        fun onClick(item: Model)
        fun onRightSwipe(item: Model)
        fun onLeftSwipe(item: Model)
        fun onPlay(item: Model, external: Boolean)
        fun onShowChannel(item: Model)
        fun onStar(item: Model)
        fun onShare(item: Model)
        fun onView(item: Model)
        fun onPlayStartClick(item: Model)
    }

    data class Model constructor(
        override val id: Long,
        val index: Int,
        val url: String,
        val type: MediaDomain.MediaTypeDomain,
        val title: String,
        val duration: String,
        val positon: String,
        val thumbNailUrl: String?,
        val progress: Float, // 0..1
        val published: String,
        val watchedSince: String,
        val isWatched: Boolean,
        val starred: Boolean,
        val platform: PlatformDomain
    ) : ItemBaseModel(id)

    data class State constructor(var item: Model? = null)
}
