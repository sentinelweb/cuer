package uk.co.sentinelweb.cuer.app.ui.support

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.MviStore.*
import uk.co.sentinelweb.cuer.app.util.link.LinkExtractor
import uk.co.sentinelweb.cuer.app.util.link.YoutubeUrl
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain

class SupportStoreFactory constructor(
    private val storeFactory: StoreFactory = DefaultStoreFactory(),
    log: LogWrapper,
    private val prefs: MultiPlatformPreferencesWrapper,
    private val linkExtractor: LinkExtractor,
) {

    init {
        log.tag(this)
    }

    private sealed class Result {
        data class Load(val media: MediaDomain, val links: List<LinkDomain>?) : Result()
    }

    private sealed class Action

    private inner class ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.Load -> copy(media = result.media, links = result.links)
            }
    }

    private inner class ExecutorImpl : SuspendExecutor<Intent, Action, State, Result, Label>() {

        override suspend fun executeIntent(intent: Intent, getState: () -> State) =
            when (intent) {
                is Intent.Open -> when (intent.link) {
                    is LinkDomain.UrlLinkDomain -> publish(Label.Open(intent.link))
                    is LinkDomain.CryptoLinkDomain -> publish(Label.Crypto(intent.link))
                }
                is Intent.Load -> intent.media.description
                    ?.let { linkExtractor.extractLinks(it).toMutableList() }
                    ?.let { list -> // add channel url (customUrl doesn't work)
                        intent.media.channelData
                            .let {
                                list.add(
                                    linkExtractor.mapUrlToLinkDomain(// todo map properly
                                        object : MatchResult {
                                            override val groupValues = listOf<String>()
                                            override val groups: MatchGroupCollection
                                                get() = TODO("Not yet implemented")
                                            override val range: IntRange = 0..0
                                            override val value: String =
                                                YoutubeUrl.channelPlatformIdUrl(it)
                                            override fun next(): MatchResult? {
                                                TODO("Not yet implemented")
                                            }
                                        }
                                    )
                                ); list
                            }
                    }
                    ?.let { list -> list.distinctBy { it.address } }
                    ?.let { dispatch(Result.Load(intent.media, it)) }
                    ?: dispatch(Result.Load(intent.media, listOf()))
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
