package uk.co.sentinelweb.cuer.app.ui.browse

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.*
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.ext.buildIdLookup
import uk.co.sentinelweb.cuer.domain.ext.buildParentLookup

class BrowseStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory,
    private val repository: BrowseRepository,
) {
    private sealed class Result {
        class SetCategory(val category: CategoryDomain) : Result()
        class LoadCatgeories(val root: CategoryDomain) : Result()
        object Display : Result()
    }

    private sealed class Action {
        object Init : Action()
    }


    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.Display -> copy()
                is Result.SetCategory -> copy(currentCategory = result.category)
                is Result.LoadCatgeories -> copy(currentCategory = result.root,
                    categoryLookup = result.root.buildIdLookup(),
                    parentLookup = result.root.buildParentLookup()
                )
            }
    }

    private class BootstrapperImpl() : SuspendBootstrapper<Action>() {
        override suspend fun bootstrap() {
            dispatch(Action.Init)
        }
    }

    private inner class ExecutorImpl() : SuspendExecutor<Intent, Action, State, Result, Label>() {
        override suspend fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                Action.Init -> loadCategories()
            }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.ClickCategory -> {
                    getState().categoryLookup.get(intent.id)
                        ?.apply { dispatch(Result.SetCategory(category = this)) }
                        ?: apply { publish(Label.Error("")) }
                    Unit
                }
                is Intent.Display -> dispatch(Result.Display)
                is Intent.Up -> {
                    getState().parentLookup.get(getState().currentCategory)
                        ?.apply { dispatch(Result.SetCategory(category = this)) }
                        ?: apply { publish(Label.TopReached) }
                    Unit
                }
            }

        private fun loadCategories() {
            dispatch(Result.LoadCatgeories(repository.loadAll()))
        }
    }


    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "BrowseStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl
        ) {}
}