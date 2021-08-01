package uk.co.sentinelweb.cuer.app.ui.browse

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.domain.CategoryDomain

class BrowseContract {

    interface MviStore : Store<MviStore.Intent, MviStore.State, Nothing> {
        sealed class Intent {
            object Display : Intent()
            data class ClickCategory(val id: Long) : Intent()
        }

        data class State constructor(
            val currentCategory: Long? = null,
            val categories: List<CategoryDomain> = listOf(),
        )
    }

    interface View : MviView<View.Model, View.Event> {
        //        suspend fun processLabel(label: PlayerContract.MviStore.Label)
        data class Model(
            val categories: List<CategoryModel>,
        )

        data class CategoryModel(
            val id: Long,
            val title: String,
            val description: String?,
            val thumbNailUrl: String?,
            val subCategories: List<CategoryModel>,
            val videoCount: Int,
        )

        sealed class Event {
            object OnResume : Event()
            data class CategoryClicked(val id: Long) : Event()
        }
    }
}