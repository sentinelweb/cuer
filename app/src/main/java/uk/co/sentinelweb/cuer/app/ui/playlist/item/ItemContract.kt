package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.text.SpannableString
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.navigation.fragment.FragmentNavigator
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
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
        fun showProgress(live: Boolean)
        fun makeTransitionExtras(): FragmentNavigator.Extras
        fun setDurationBackground(@ColorRes infoTextBackgroundColor: Int)
        fun setPlayIcon(@DrawableRes icon: Int)
        fun dismissMenu()
        fun setShowOverflow(showOverflow: Boolean)
    }

    interface Presenter {
        fun doClick()
        fun doPlay(external: Boolean)
        fun doShowChannel()
        fun doStar()
        fun doShare()
        fun doRelated()
        fun doIconClick()
        fun doPlayStartClick()
        fun doGotoPlaylist()
        fun updateProgress()
        fun isViewForId(id: Long): Boolean
        fun isStarred(): Boolean
        fun isCompositePlaylist(): Boolean
    }

    interface External : ItemBaseContract.ItemPresenterBase {
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
        fun onRelated(item: Model)
        fun onShare(item: Model)
        fun onItemIconClick(item: Model)
        fun onPlayStartClick(item: Model)
        fun onGotoPlaylist(item: Model)
    }

    data class Model(
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
        val platform: PlatformDomain,
        val isLive: Boolean,
        val isUpcoming: Boolean,
        @ColorRes val infoTextBackgroundColor: Int,
        val canEdit: Boolean,
        val playlistName: String?,
        val canDelete: Boolean,
        val canReorder: Boolean,
        val showOverflow: Boolean
    ) : ItemBaseModel(id)

    data class State constructor(var item: Model? = null)
}
