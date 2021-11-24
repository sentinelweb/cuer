package uk.co.sentinelweb.cuer.app.ui.browse

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.PlatformIdListFilter
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.TitleFilter
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.flatOptions
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.*
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPrefences.BROWSE_CAT_TITLE
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.ext.buildIdLookup
import uk.co.sentinelweb.cuer.domain.ext.buildParentLookup

class BrowseStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory,
    private val repository: BrowseRepository,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val browseStrings: BrowseContract.BrowseStrings,
    private val log: LogWrapper,
    private val prefs: MultiPlatformPreferencesWrapper,
    private val recentCategories: BrowseRecentCategories,
) {

    init {
        log.tag(this)
    }

    private sealed class Result {
        class SetCategory(val category: CategoryDomain) : Result()
        class SetCategoryByTitle(val title: String) : Result()
        class LoadCatgeories(val root: CategoryDomain) : Result()
        object Display : Result()
        class SetOrder(val order: BrowseContract.Order) : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private inner class ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.Display -> this
                is Result.SetCategory -> copy(currentCategory = result.category)
                is Result.SetOrder -> copy(order = result.order)
                is Result.SetCategoryByTitle -> {
                    this.categoryLookup.values
                        .find { it.title == result.title }
                        ?.let { copy(currentCategory = it) }
                        ?: this
                }
                is Result.LoadCatgeories -> {
                    val categoryLookup1 = result.root.buildIdLookup()
                    copy(
                        currentCategory = result.root,
                        categoryLookup = categoryLookup1,
                        parentLookup = result.root.buildParentLookup(),
                        recent = recentCategories
                            .getRecent()
                            .reversed()
                            .mapNotNull { recentTitle ->
                                categoryLookup1.values
                                    .find { it.title == recentTitle }
                            }
                    )
                }
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
                        ?.also { cat ->
                            if (cat.subCategories.size > 0) {
                                prefs.putString(
                                    BROWSE_CAT_TITLE,
                                    cat.title
                                ) //fixme: change to some built id(maybe include parent)
                            }
                            cat.platformId
                                ?.takeIf { intent.forceItem || cat.subCategories.isEmpty() }
                                ?.let { checkPlatformId(cat, getState) }
                                ?: run {
                                    if (cat.subCategories.isNotEmpty()) {
                                        dispatch(Result.SetCategory(category = cat))
                                    } else {
                                        publish(Label.Error(browseStrings.errorNoPlaylistConfigured))
                                    }
                                }
                        }
                        ?: apply { publish(Label.Error(browseStrings.errorNoCatWithID(intent.id))) }
                    Unit
                }
                is Intent.Display -> dispatch(Result.Display)
                is Intent.SetOrder -> dispatch(Result.SetOrder(intent.order))
                is Intent.Up -> {
                    getState().parentLookup.get(getState().currentCategory)
                        ?.apply {
                            prefs.putString(BROWSE_CAT_TITLE, this.title)
                            dispatch(Result.SetCategory(category = this))
                        }
                        ?: apply {
                            prefs.remove(BROWSE_CAT_TITLE)
                            publish(Label.TopReached)
                        }
                    Unit
                }
                Intent.ActionSettings -> publish(Label.ActionSettings)
                Intent.ActionSearch -> publish(Label.ActionSearch)
            }

        private suspend fun checkPlatformId(
            cat: CategoryDomain,
            getState: () -> State
        ) =
            (playlistOrchestrator.loadList(
                PlatformIdListFilter(ids = listOf(cat.platformId!!)),
                LOCAL.flatOptions()
            )
                .takeIf { it.isNotEmpty() }
                ?.let { it[0].id }
                ?.also {
                    recentCategories.addRecent(cat)
                    publish(Label.OpenLocalPlaylist(it))
                }
                ?: also {
                    val catParent = getTopLevelCategory(cat, getState)
                    val parentId =
                        playlistOrchestrator.loadList(
                            TitleFilter(title = catParent.title),
                            LOCAL.flatOptions()
                        )
                            .takeIf { it.isNotEmpty() }
                            ?.get(0)?.id
                    recentCategories.addRecent(cat)
                    publish(Label.AddPlaylist(cat, parentId))
                })

        private fun getTopLevelCategory(
            cat: CategoryDomain,
            getState: () -> State,
        ): CategoryDomain {
            var catParent = cat
            while (getState().parentLookup[catParent] != null) {
                // check next parent is not root
                val nextParent = getState().parentLookup[catParent]
                if (getState().parentLookup[nextParent] == null) break
                catParent =
                    getState().parentLookup[catParent]
                        ?: throw IllegalStateException("parent lookup error")
            }
            return catParent
        }

        private suspend fun loadCategories(): Unit = kotlin.runCatching {
            repository.loadAll()
            // .apply { log.d(root.buildIdLookup().values.joinToString("\n") { "${it.title} - ${it.image?.url}" })*/ }
        }.onSuccess {
            dispatch(Result.LoadCatgeories(it))
            prefs.getString(BROWSE_CAT_TITLE, null)
                ?.also { dispatch(Result.SetCategoryByTitle(it)) }
        }
            .onFailure { log.e("browse load fail", it) }
            .let { }
    }

    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "BrowseStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl()
        ) {}


}