package uk.co.sentinelweb.cuer.app.ui.support

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain

interface SupportContract {
    interface MviStore :
        Store<MviStore.Intent, MviStore.State, MviStore.Label> {

        sealed class Intent {
            data class Load(val media: MediaDomain) : MviStore.Intent()
            data class Open(val url: String) : Intent()
        }

        sealed class Label {
            data class Open(val url: String) : Label()
        }

        data class State constructor(
            val links: List<LinkDomain> = listOf()
        )
    }

    interface View : MviView<View.Model, View.Event> {
        suspend fun processLabel(label: MviStore.Label)

        data class Model(
            val links: List<Link>
        ) {
            data class Link constructor(
                val title: String,
                val link: String,
            )
        }

        sealed class Event {
            data class OnLinkClicked(val link: Model.Link) : Event()
            data class Load(val media: MediaDomain) : Event()
        }
    }

    interface Strings {

    }
}