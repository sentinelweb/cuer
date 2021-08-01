package uk.co.sentinelweb.cuer.app.ui.browse

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.Intent
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.State
import uk.co.sentinelweb.cuer.domain.CategoryDomain

class BrowseStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory,
    private val repository: BrowseRepository,
) {
    private sealed class Result {
        class SetCategory(val id: Long) : Result()
        class LoadCatgeories(val categories: List<CategoryDomain>) : Result()
        object Display : Result()
    }

    private sealed class Action {
        object Init : Action()
    }


    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.Display -> copy()
                is Result.SetCategory -> copy(currentCategory = result.id)
                is Result.LoadCatgeories -> copy(categories = result.categories)
            }
    }

    private class BootstrapperImpl() : SuspendBootstrapper<Action>() {
        override suspend fun bootstrap() {
            dispatch(Action.Init)
        }
    }

    private inner class ExecutorImpl() : SuspendExecutor<Intent, Action, State, Result, Nothing>() {
        override suspend fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                Action.Init -> loadCategories()
            }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.ClickCategory -> dispatch(Result.SetCategory(id = intent.id))
                is Intent.Display -> dispatch(Result.Display)
            }

        private fun loadCategories() {
            dispatch(Result.LoadCatgeories(repository.loadAll()))
        }
    }


    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "BrowseStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl
        ) {}
}