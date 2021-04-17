package uk.co.sentinelweb.cuer.app.ui.search

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.domain.SearchDomain

interface SearchContract {

    data class State(
        var search: SearchDomain = SearchDomain()
    )

    data class Model(
        val text: String = "",
        val isLocal: Boolean = true,
        val localParams: LocalModel = LocalModel(),
        val remoteParams: SearchDomain.RemoteParms = SearchDomain.RemoteParms()
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