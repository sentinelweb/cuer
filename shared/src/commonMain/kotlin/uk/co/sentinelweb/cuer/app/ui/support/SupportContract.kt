package uk.co.sentinelweb.cuer.app.ui.support

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.view.MviView
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.LinkDomain.Category.*
import uk.co.sentinelweb.cuer.domain.MediaDomain

interface SupportContract {
    interface MviStore : Store<MviStore.Intent, MviStore.State, MviStore.Label> {

        sealed class Intent {
            data class Load(val media: MediaDomain) : MviStore.Intent()
            data class Open(val link: LinkDomain) : Intent()
        }

        sealed class Label {
            data class Open(val link: LinkDomain.UrlLinkDomain) : Label()
            data class Crypto(val link: LinkDomain.CryptoLinkDomain) : Label()
        }

        data class State constructor(
            val media: MediaDomain? = null,
            val links: List<LinkDomain>? = null
        )
    }

    interface View : MviView<View.Model, View.Event> {

        suspend fun processLabel(label: MviStore.Label)

        data class Model(
            val title:String?=null,
            val links: Map<LinkDomain.Category, List<Link>>? = null,
            val isInitialised: Boolean = false
        ) {
            data class Link(
                val title: String,
                val link: String,
                val index: Int,
                val category: LinkDomain.Category,
                val domain: LinkDomain,
            )
        }

        sealed class Event {
            data class OnLinkClicked(val link: Model.Link) : Event()
            data class Load(val media: MediaDomain) : Event()
        }

        companion object {
            val CATEGORY_ORDER = listOf(DONATE, CRYPTO, VIDEO, SOCIAL, PODCAST, OTHER)
        }
    }

    interface Strings {

    }
}