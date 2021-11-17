package uk.co.sentinelweb.cuer.app.ui.playlists.item

import android.text.Spannable
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.navigation.fragment.FragmentNavigator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseModel
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface ItemContract {

    enum class ItemType {
        ROW, HEADER, LIST, TILE
    }

    interface ItemView {
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
        fun isStarred(): Boolean
    }

    interface External<in T : Model> : ItemBaseContract.ItemPresenterBase {
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
        fun onClick(item: Model, sourceView: ItemView)
        fun onRightSwipe(item: Model)
        fun onLeftSwipe(item: Model)
        fun onPlay(item: Model, external: Boolean, sourceView: ItemView)
        fun onStar(item: Model)
        fun onShare(item: Model)
        fun onMerge(item: Model)
        fun onImageClick(item: Model, sourceView: ItemView)
        fun onEdit(item: Model, sourceView: ItemView)
    }

    data class State constructor(
        var item: Model.ItemModel? = null
    )

    data class ListState constructor(
        var presenters: MutableList<External<Model.ItemModel>> = mutableListOf()
    )

    sealed class Model(override val id: Long) : ItemBaseModel(id) {

        data class HeaderModel(
            override val id: Long,
            val title: String,
        ) : Model(id)

        data class ListModel(
            override val id: Long,
            val items: List<ItemModel>,
        ) : Model(id)

        data class ItemModel(
            override val id: Long,// todo OrchestratorContract.Identifier
            val title: String,
            val checkIcon: Boolean,
            val thumbNailUrl: String?,
            val starred: Boolean,
            val count: Int,
            val newItems: Int,
            val loopMode: PlaylistDomain.PlaylistModeDomain,
            val type: PlaylistDomain.PlaylistTypeDomain,
            val platform: PlatformDomain?,
            val showOverflow: Boolean,
            val source: OrchestratorContract.Source,
            val canPlay: Boolean,
            val canEdit: Boolean,
            val canDelete: Boolean,
            val canLaunch: Boolean,
            val canShare: Boolean,
            val watched: Boolean,
            val pinned: Boolean,
            val default: Boolean,
            val depth: Int
        ) : Model(id)
    }

    companion object {
        const val ID_APP_HEADER = -1001L
        const val ID_APP_LIST = -1002L
        const val ID_RECENT_HEADER = -1003L
        const val ID_RECENT_LIST = -1004L
        const val ID_STARRED_HEADER = -1005L
        const val ID_STARRED_LIST = -1006L
        const val ID_ALL_HEADER = -1007L
    }

}
