package uk.co.sentinelweb.cuer.app.ui.playlists.item

import android.text.Spannable
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.navigation.fragment.FragmentNavigator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract

interface ItemContract {

    enum class ItemType {
        ROW, HEADER, LIST, TILE
    }

    interface ItemView : PlaylistsItemMviContract.ItemPassView {
        fun makeTransitionExtras(): FragmentNavigator.Extras
    }

    interface View : ItemView {
        val type: ItemType
        fun setTopText(text: Spannable)
        fun setBottomText(text: Spannable)
        fun setIconResource(@DrawableRes iconRes: Int)
        fun setCheckedVisible(checked: Boolean)
        fun setPresenter(itemPresenter: Presenter)
        fun setIconUrl(url: String)
        fun showOverflow(showOverflow: Boolean)
        fun setVisible(b: Boolean)
        fun setDepth(depth: Int)
        fun setTransitionData(s: String, s1: String)
    }

    interface Presenter {
        fun doClick()
        fun doPlay(external: Boolean)
        fun doStar()
        fun doShare()
        fun canEdit(): Boolean
        fun canShare(): Boolean
        fun canPlay(): Boolean
        fun canLaunch(): Boolean
        fun doMerge()
        fun doImageClick()
        fun doEdit()
        fun doDelete()
        fun isStarred(): Boolean
    }

    interface External<in T : PlaylistsItemMviContract.Model> : ItemBaseContract.ItemPresenterBase {
        fun update(item: T, current: OrchestratorContract.Identifier<*>?)
        fun doLeft()
        fun doRight()
        var parentId: Long?
    }

    interface HeaderView : ItemView {
        val root: android.view.View
        fun setTitle(title: String)
    }

    interface ListView : ItemView {
        val parent: ViewGroup
        val root: android.view.View
        fun setPresenter(listPresenter: ListPresenter)
        fun clear()
    }

    interface ListPresenter {

    }

    interface Interactions {
        fun onClick(item: PlaylistsItemMviContract.Model, sourceView: ItemView)
        fun onRightSwipe(item: PlaylistsItemMviContract.Model)
        fun onLeftSwipe(item: PlaylistsItemMviContract.Model)
        fun onPlay(item: PlaylistsItemMviContract.Model, external: Boolean, sourceView: ItemView)
        fun onStar(item: PlaylistsItemMviContract.Model)
        fun onShare(item: PlaylistsItemMviContract.Model)
        fun onMerge(item: PlaylistsItemMviContract.Model)
        fun onImageClick(item: PlaylistsItemMviContract.Model, sourceView: ItemView)
        fun onEdit(item: PlaylistsItemMviContract.Model, sourceView: ItemView)
        fun onDelete(item: PlaylistsItemMviContract.Model, sourceView: ItemView)
    }

    data class State constructor(
        var item: PlaylistsItemMviContract.Model.Item? = null
    )

    data class ListState constructor(
        var presenters: MutableList<External<PlaylistsItemMviContract.Model.Item>> = mutableListOf()
    )
}
