package uk.co.sentinelweb.cuer.app.ui.browse

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.State
import uk.co.sentinelweb.cuer.domain.CategoryDomain

class BrowseStoreFactory constructor(
    private val storeFactory: StoreFactory,
) {
    private sealed class Result {
        class SetCategory(val id: Long) : Result()
        class LoadCatgeories(val categories: List<CategoryDomain>) : Result()
    }

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.SetCategory -> copy(currentCategory = result.id)
                is Result.LoadCatgeories -> copy(categories = result.categories)
            }
    }

    private inner class ExecutorImpl(
    ) : SuspendExecutor<Intent, Nothing, State, Result, Nothing>() {
        override suspend fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                Intent.Load -> loadCategories()
                is Intent.ClickCategory -> dispatch(Result.SetCategory(id = intent.id))
            }

        private fun loadCategories() {
            dispatch(Result.LoadCatgeories(buildTestList()))
        }
    }

    private fun buildTestList() = listOf(
        // -1 = build id in loader
        CategoryDomain(
            id = -1,
            title = "Philosophy",
            subCategories = listOf(
                CategoryDomain(id = -1, title = "Greek", subCategories = listOf(
                    CategoryDomain(id = -1, title = "Plato"),
                    CategoryDomain(id = -1, title = "Aristotle"),
                    CategoryDomain(id = -1, title = "Zeno"),

                    )
                ),
                CategoryDomain(id = -1, title = "Renaissance", subCategories = listOf(
                    CategoryDomain(id = -1, title = "Hume"),
                    CategoryDomain(id = -1, title = "Decartes"),
                )
                ),
                CategoryDomain(id = -1, title = "Existentialist", subCategories = listOf(
                    CategoryDomain(id = -1, title = "Satre"),
                )
                ),
                CategoryDomain(id = -1, title = "Poststructuralist", subCategories = listOf(
                    CategoryDomain(id = -1, title = "Baurillard"),
                    CategoryDomain(id = -1, title = "Deleueze"),
                )
                ),
            )
        ),
        CategoryDomain(
            id = -1,
            title = "Meditation",
            subCategories = listOf(
                CategoryDomain(id = -1, title = "Vipassana"),
                CategoryDomain(id = -1, title = "Zen"),
                CategoryDomain(id = -1, title = "Trancendental"),
                CategoryDomain(id = -1, title = "Chakra"),
                CategoryDomain(id = -1, title = "Yoga"),
                CategoryDomain(id = -1, title = "Jain"),
            )
        )
    )

    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "BrowseStore",
            initialState = State(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl
        ) {}
}