package uk.co.sentinelweb.cuer.app.ui.search

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.domain.PlatformDomain

interface SearchContract {

    data class State(
        val text: String = "",
        val isLocal: Boolean = true,
        val localParams: LocalParms = LocalParms(),
        val remoteParams: RemoteParms = RemoteParms()
    )

    data class LocalParms(
        val isWatched: Boolean? = null,
        val playlists: MutableList<Long> = mutableListOf(),
        val isLive: Boolean? = null
    )

    data class RemoteParms(
        val platform: PlatformDomain = PlatformDomain.YOUTUBE,
        val relatedToPlatformId: String? = null,
        val channelPlatformId: String? = null,
        val isLive: Boolean? = null
    )

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<SearchBottomSheetFragment>()) {
                viewModel {
                    SearchViewModel(
                        state = get(),
                        log = get()
                    )
                }
                scoped { State() }

            }
        }

    }
}