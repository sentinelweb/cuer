package uk.co.sentinelweb.cuer.app.ui.search

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.EnumValuesDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.domain.PlatformDomain
import uk.co.sentinelweb.cuer.domain.SearchLocalDomain
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain

interface SearchContract {

    data class State(
        var searchType: SearchType = SearchType.LOCAL,
        var local: SearchLocalDomain = SearchLocalDomain(),
        var remote: SearchRemoteDomain = SearchRemoteDomain()
    )

    data class Model(
        val type: String,
        val otherType: String,
        val text: String?,
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
        val relatedTo: String?,
        val channelPlatformId: String?,
        val isLive: Boolean,
        val fromDate: String?,
        val toDate: String?,
        val order: SearchRemoteDomain.Order
    )

    enum class SearchType {
        LOCAL, REMOTE
    }

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<SearchBottomSheetFragment>()) {
                viewModel {
                    SearchViewModel(
                        state = get(),
                        log = get(),
                        mapper = get(),
                        prefsWrapper = get(),
                        timeStampMapper = get(),
                        timeProvider = get()
                    )
                }
                scoped { State() }
                scoped { navigationMapper(true, getSource<Fragment>().requireActivity() as AppCompatActivity) }
                scoped { EnumValuesDialogCreator(getSource<Fragment>().requireContext()) }
            }
            factory { SearchMapper(get(), get()) }
        }

    }
}