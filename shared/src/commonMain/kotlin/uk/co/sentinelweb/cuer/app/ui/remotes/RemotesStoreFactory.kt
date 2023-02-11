package uk.co.sentinelweb.cuer.app.ui.remotes

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistStatsOrchestrator
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.*
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

// fixme: 'SuspendBootstrapper<Action : Any>' is deprecated. Please use CoroutineBootstrapper
class RemotesStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory(),
    private val repository: RemotesRepository,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistStatsOrchestrator: PlaylistStatsOrchestrator,
    private val strings: StringDecoder,
    private val log: LogWrapper,
    private val prefs: MultiPlatformPreferencesWrapper
) {

    init {
        log.tag(this)
    }

    private sealed class Result {
//        data class SetCategory(val category: CategoryDomain) : Result()
//        data class SetCategoryByTitle(val title: String) : Result()
//        data class LoadCatgeories(
//            val root: CategoryDomain,
//            val existingPlaylists: List<PlaylistDomain>,
//            val existingPlaylistStats: List<PlaylistStatDomain>
//        ) : Result()
//
//        object Display : Result()
//        data class SetOrder(val order: BrowseContract.Order) : Result()
    }

    private sealed class Action {
        object Init : Action()
    }

    private inner class ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State = this
//            when (result) {
//                is Result.Display -> this
//                is Result.SetCategory -> copy(currentCategory = result.category)
//                is Result.SetOrder -> copy(order = result.order)
//                is Result.SetCategoryByTitle -> {
//                    this.categoryLookup.values
//                        .find { it.title == result.title }
//                        ?.let { copy(currentCategory = it) }
//                        ?: this
//                }
//
//                is Result.LoadCatgeories -> {
//                    val categoryLookup1 = result.root.buildIdLookup()
//                    copy(
//                        currentCategory = result.root,
//                        categoryLookup = categoryLookup1,
//                        parentLookup = result.root.buildParentLookup(),
//                        existingPlaylists = result.existingPlaylists,
//                        existingPlaylistStats = result.existingPlaylistStats,
//                        recent = recentCategories
//                            .getRecent()
//                            .reversed()
//                            .mapNotNull { recentTitle ->
//                                categoryLookup1.values
//                                    .find { it.title == recentTitle }
//                            },
//                    )
//                }
//            }
    }

    private class BootstrapperImpl() : SuspendBootstrapper<Action>() {
        override suspend fun bootstrap() {
            dispatch(Action.Init)
        }
    }

    private inner class ExecutorImpl() : SuspendExecutor<Intent, Action, State, Result, Label>() {
        override suspend fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                Action.Init -> load()
            }

        private fun load() {
            log.d("load")
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) = Unit
//            when (intent) {
//                is Intent.ClickCategory -> { // todo extract inside closure
//                    getState().categoryLookup.get(intent.id)
//                        ?.also { cat ->
//                            if (cat.subCategories.size > 0) {
//                                prefs.putString(
//                                    BROWSE_CAT_TITLE,
//                                    cat.title
//                                ) //fixme: change to some built id(maybe include parent)
//                            }
//                            cat.platformId
//                                ?.takeIf { intent.forceItem || cat.subCategories.isEmpty() }
//                                ?.let { checkPlatformId(cat, getState) }
//                                ?: run {
//                                    if (cat.subCategories.isNotEmpty()) {
//                                        dispatch(Result.SetCategory(category = cat))
//                                    } else {
//                                        publish(Label.Error(browseStrings.errorNoPlaylistConfigured))
//                                    }
//                                }
//                        }
//                        ?: apply { publish(Label.Error(browseStrings.errorNoCatWithID(intent.id))) }
//                    Unit
//                }
//
//                is Intent.Display -> dispatch(Result.Display)
//                is Intent.SetOrder -> dispatch(Result.SetOrder(intent.order))
//                is Intent.Up -> {
//                    getState().parentLookup.get(getState().currentCategory)
//                        ?.apply {
//                            prefs.putString(BROWSE_CAT_TITLE, this.title)
//                            dispatch(Result.SetCategory(category = this))
//                        }
//                        ?: apply {
//                            prefs.remove(BROWSE_CAT_TITLE)
//                            publish(Label.TopReached)
//                        }
//                    Unit
//                }
//
//                Intent.ActionSettings -> publish(Label.ActionSettings)
//                Intent.ActionPasteAdd -> publish(Label.ActionPasteAdd)
//                Intent.ActionSearch -> publish(Label.ActionSearch)
//                Intent.ActionHelp -> publish(Label.ActionHelp)
//            }


    }

    fun create(): MviStore =
        object : MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RemnotesStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl()
        ) {}


}