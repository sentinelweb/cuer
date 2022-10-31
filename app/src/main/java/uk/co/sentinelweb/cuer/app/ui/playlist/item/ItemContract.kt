package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.text.SpannableString
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.navigation.fragment.FragmentNavigator
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseModel
import uk.co.sentinelweb.cuer.app.ui.resources.ActionResources
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain

interface ItemContract {

    interface View {
        val itemView: android.view.View
        val rightSwipeView: android.view.View
        val leftSwipeView: android.view.View
        fun resetBackground()
        fun setTopText(text: SpannableString)
        fun setBottomText(text: SpannableString)
        fun setImageResource(@DrawableRes iconRes: Int)
        fun setImageUrl(url: String)
        fun setThumbResource(@DrawableRes iconRes: Int)
        fun setThumbUrl(url: String)
        fun setChannelImageResource(@DrawableRes iconRes: Int)
        fun setChannelImageUrl(url: String)
        fun setCheckedVisible(checked: Boolean)
        fun setPresenter(itemPresenter: Presenter)
        fun setBackground(@ColorRes backgroundColor: Int)
        fun setDuration(text: String)
        fun setProgress(ratio: Float)
        fun showProgress(live: Boolean)
        fun makeTransitionExtras(): FragmentNavigator.Extras
        fun setDurationBackground(@ColorRes infoTextBackgroundColor: Int)
        fun setPlayIcon(@DrawableRes icon: Int)
        fun dismissMenu()
        fun setShowOverflow(showOverflow: Boolean)
        fun isViewForId(id: Long): Boolean
        fun setDeleteResources(deleteResources: ActionResources?)
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
        fun doAuthorClick()
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
        val imageUrl: String?,
        val thumbUrl: String?,
        val channelImageUrl: String?,
        val progress: Float, // 0..1
        val published: String,
        val watchedSince: String,
        val isWatched: Boolean,
        val isStarred: Boolean,
        val platform: PlatformDomain,
        val isLive: Boolean,
        val isUpcoming: Boolean,
        @ColorRes val infoTextBackgroundColor: Int,
        val canEdit: Boolean,
        val playlistName: String?,
        val canDelete: Boolean,
        val canReorder: Boolean,
        val showOverflow: Boolean,
        val deleteResources: ActionResources?
    ) : ItemBaseModel(id)

    data class State constructor(var item: Model? = null)
}
