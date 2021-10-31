package uk.co.sentinelweb.cuer.app.ui.browse

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.CategoryDomain.Companion.EMPTY_CATEGORY
import uk.co.sentinelweb.cuer.domain.PlatformDomain

class BrowseContract {

    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {
        sealed class Intent {
            object Display : Intent()
            data class ClickCategory(val id: Long) : Intent()
            object Up : Intent()
            object ActionSettings : Intent()
        }

        sealed class Label {
            object None : Label()
            data class Error(val message: String, val exception: Throwable? = null) : Label()
            object TopReached : Label()
            object ActionSettings : Label()
            data class AddPlaylist(
                val platformId: String,
                val platform: PlatformDomain = PlatformDomain.YOUTUBE,
                val parentId: Long? = null,
            ) : Label()

            data class OpenLocalPlaylist(val id: Long, val play: Boolean = false) : Label()
        }

        data class State(
            val currentCategory: CategoryDomain = EMPTY_CATEGORY,
            val categoryLookup: Map<Long, CategoryDomain> = mapOf(),
            val parentLookup: Map<CategoryDomain, CategoryDomain> = mapOf(),
            val recent: List<CategoryDomain> = listOf(),
        )
    }

    interface View : MviView<View.Model, View.Event> {

        suspend fun processLabel(label: MviStore.Label)

        data class Model(
            val title: String,
            val categories: List<CategoryModel>,
            val recent: CategoryModel?,
            val isRoot: Boolean,
        )

        data class CategoryModel(
            val id: Long,
            val title: String,
            val description: String?,
            val thumbNailUrl: String?,
            val subCategories: List<CategoryModel>,
            val subCount: Int,
            val isPlaylist: Boolean,
        )

        sealed class Event {
            object OnResume : Event()
            object UpClicked : Event()
            object ActionSettingsClick : Event()
            data class CategoryClicked(val id: Long) : Event()
        }
    }

    interface BrowseStrings {
        val recent: String
        val errorNoPlaylistConfigured: String
        fun errorNoCatWithID(id: Long): String
    }
}