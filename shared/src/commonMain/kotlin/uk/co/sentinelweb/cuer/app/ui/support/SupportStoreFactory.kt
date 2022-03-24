package uk.co.sentinelweb.cuer.app.ui.support

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.MviStore.*
import uk.co.sentinelweb.cuer.app.util.link.LinkExtractor
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain

class SupportStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory,
    private val log: LogWrapper,
    private val prefs: MultiPlatformPreferencesWrapper,
    private val linkExtractor: LinkExtractor,
) {

    init {
        log.tag(this)
    }

    private sealed class Result {
        class Load(val links: List<LinkDomain>) : Result()
    }

    private sealed class Action

    private inner class ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.Load -> copy(links = result.links)
            }
    }

    private inner class ExecutorImpl() :
        SuspendExecutor<Intent, Action, State, Result, Label>() {

        override suspend fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.Open -> publish(Label.Open(intent.url))
                is Intent.Load -> intent.media.description
                    ?.let { dispatch(Result.Load(linkExtractor.extractLinks(it))) }
                    ?: dispatch(Result.Load(listOf()))
            }
    }

    fun create(): SupportContract.MviStore =
        object : SupportContract.MviStore, Store<Intent, State, Label> by storeFactory.create(
            name = "SupportStore",
            initialState = State(),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl()
        ) {}
}
