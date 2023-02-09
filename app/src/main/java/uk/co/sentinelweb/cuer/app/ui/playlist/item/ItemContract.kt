package uk.co.sentinelweb.cuer.app.ui.playlist.item

import android.text.SpannableString
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.navigation.fragment.FragmentNavigator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract.Model.Item
import uk.co.sentinelweb.cuer.domain.GUID

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
        fun isViewForId(id: Identifier<GUID>): Boolean
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
        fun isViewForId(id: Identifier<GUID>): Boolean
        fun isStarred(): Boolean
        fun isCompositePlaylist(): Boolean
        fun doAuthorClick()
    }

    interface External : ItemBaseContract.ItemPresenterBase {
        fun update(item: Item, highlightPlaying: Boolean)
        fun doLeft()
        fun doRight()
    }

    interface Interactions {
        fun onClick(item: Item)
        fun onRightSwipe(item: Item)
        fun onLeftSwipe(item: Item)
        fun onPlay(item: Item, external: Boolean)
        fun onShowChannel(item: Item)
        fun onStar(item: Item)
        fun onRelated(item: Item)
        fun onShare(item: Item)
        fun onItemIconClick(item: Item)
        fun onPlayStartClick(item: Item)
        fun onGotoPlaylist(item: Item)
    }

    data class State constructor(var item: Item? = null)
}
