package uk.co.sentinelweb.cuer.app.ui.remotes

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistStatsOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.*
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain

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
        data class SetNodes(val nodes: List<NodeDomain>) : Result()
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
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.SetNodes -> copy(nodes = result.nodes)

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
            }
    }

    private class BootstrapperImpl() : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.Init)
        }
    }

    private inner class ExecutorImpl() : CoroutineExecutor<Intent, Action, State, Result, Label>() {
        override fun executeAction(action: Action, getState: () -> State) =
            when (action) {
                Action.Init -> testLoad()
            }

        override fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {

//
                Intent.ActionSettings -> publish(Label.ActionSettings)
                Intent.ActionPasteAdd -> publish(Label.ActionPasteAdd)
                Intent.ActionSearch -> publish(Label.ActionSearch)
                Intent.ActionHelp -> publish(Label.ActionHelp)
                Intent.SendPing -> log.d("send ping")
                Intent.Up -> publish(Label.Up)
            }

        private fun testLoad() {
            dispatch(
                Result.SetNodes(
                    listOf(
                        NodeDomain(
                            id = "".toGuidIdentifier(MEMORY),
                            ipAddress = "",
                            port = 8989,
                        )
                    )
                )
            )
        }

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