package uk.co.sentinelweb.cuer.app.ui.search

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.SearchLocalDomain
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain

interface SearchContract {

    data class State(
        var isLocal: Boolean = true,
        var local: SearchLocalDomain = SearchLocalDomain(),
        var remote: SearchRemoteDomain = SearchRemoteDomain()
    )

    data class Model(
        val text: String,
        val isLocal: Boolean,
        val localParams: LocalModel,
        val remoteParams: RemoteModel
    )

    data class LocalModel(
        val isWatched: Boolean,
        val isNew: Boolean,
        val isLive: Boolean,
        val playlists: List<ChipModel>
    )

    data class RemoteModel(
        val platform: PlatformDomain,
        val relatedToPlatformId: String?,
        val channelPlatformId: String?,
        val isLive: Boolean?
    )


    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<SearchBottomSheetFragment>()) {
                viewModel {
                    SearchViewModel(
                        state = get(),
                        log = get(),
                        mapper = get(),
                        prefsWrapper = get(named<GeneralPreferences>()),
                    )
                }
                scoped { State() }
                scoped { SearchMapper() }
                scoped { navigationMapper(true, getSource<Fragment>().requireActivity() as AppCompatActivity) }
            }
        }

    }
}