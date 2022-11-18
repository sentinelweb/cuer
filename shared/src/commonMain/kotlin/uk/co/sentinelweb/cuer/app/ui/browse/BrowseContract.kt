package uk.co.sentinelweb.cuer.app.ui.browse

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.CategoryDomain.Companion.EMPTY_CATEGORY
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

class BrowseContract {

    enum class Order { CATEGORIES, A_TO_Z }

    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
            object Display : Intent()
            data class ClickCategory(val id: Long, val forceItem: Boolean) : Intent()
            object Up : Intent()
            object ActionSettings : Intent()
            object ActionSearch : Intent()
            object ActionHelp : Intent()
            data class SetOrder(val order: Order) : Intent()
        }

        sealed class Label {
            object None : Label()
            data class Error(val message: String, val exception: Throwable? = null) : Label()
            object TopReached : Label()
            object ActionSettings : Label()
            object ActionSearch : Label()
            object ActionHelp : Label()

            data class AddPlaylist(
                val cat: CategoryDomain,
                val parentId: Long? = null,
            ) : Label()

            data class OpenLocalPlaylist(val id: Long, val play: Boolean = false) : Label()
        }

        data class State(
            val currentCategory: CategoryDomain = EMPTY_CATEGORY,
            val categoryLookup: Map<Long, CategoryDomain> = mapOf(),
            val parentLookup: Map<CategoryDomain, CategoryDomain> = mapOf(),
            val recent: List<CategoryDomain> = listOf(),
            val order: Order = Order.CATEGORIES,
            val existingPlaylists: List<PlaylistDomain> = listOf(),
            val existingPlaylistStats: List<PlaylistStatDomain> = listOf()
        )
    }

    interface View : MviView<View.Model, View.Event> {

        fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val categories: List<CategoryModel>,
            val recent: CategoryModel?,
            val isRoot: Boolean,
            val order: Order
        )

        data class CategoryModel(
            val id: Long,
            val title: String,
            val description: String?,
            val thumbNailUrl: String?,
            val subCategories: List<CategoryModel>,
            val subCount: Int,
            val isPlaylist: Boolean,
            val forceItem: Boolean,
            val existingPlaylist: Pair<PlaylistDomain, PlaylistStatDomain>?
        )

        sealed class Event {
            object OnResume : Event()
            object OnUpClicked : Event()
            object OnActionSettingsClicked : Event()
            object OnActionSearchClicked : Event()
            object OnActionHelpClicked : Event()
            data class OnCategoryClicked(val model: CategoryModel) : Event()
            data class OnSetOrder(val order: Order) : Event()
        }
    }

    interface Strings {
        val allCatsTitle: String
        val recent: String
        val errorNoPlaylistConfigured: String
        fun errorNoCatWithID(id: Long): String
    }
}