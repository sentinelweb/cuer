package uk.co.sentinelweb.cuer.app.ui.search

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface SearchContract {

    data class State(
        var text: String = "",
        var isLocal: Boolean = true,
        var localParams: LocalParms = LocalParms(),
        var remoteParams: RemoteParms = RemoteParms()
    )

    data class LocalParms(
        val isWatched: Boolean = true,
        val isNew: Boolean = true,
        val isLive: Boolean = false,
        val playlists: MutableList<PlaylistDomain> = mutableListOf()
    )

    data class RemoteParms(
        val platform: PlatformDomain = PlatformDomain.YOUTUBE,
        val relatedToPlatformId: String? = null,
        val channelPlatformId: String? = null,
        val isLive: Boolean? = null
    )

    data class Model(
        val text: String = "",
        val isLocal: Boolean = true,
        val localParams: LocalModel = LocalModel(),
        val remoteParams: RemoteParms = RemoteParms()
    )

    data class LocalModel(
        val isWatched: Boolean = true,
        val isNew: Boolean = true,
        val isLive: Boolean = false,
        val playlists: List<ChipModel> = mutableListOf()
    )


    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<SearchBottomSheetFragment>()) {
                viewModel {
                    SearchViewModel(
                        state = get(),
                        log = get(),
                        mapper = get()
                    )
                }
                scoped { State() }
                scoped { SearchMapper() }

            }
        }

    }
}