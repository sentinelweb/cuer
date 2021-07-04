package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.validator.ValidatorModel
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface PlaylistEditContract {

    data class Model(
        val titleDisplay: CharSequence,
        val titleEdit: CharSequence,
        val imageUrl: String?,
        val thumbUrl: String?,
        val starred: Boolean,
        val buttonText: String?,
        val pinned: Boolean,
        val playFromStart: Boolean,
        val default: Boolean,
        val chip: ChipModel = ChipModel.PLAYLIST_SELECT_MODEL,
        val validation: ValidatorModel?,
        @StringRes val watchAllText: Int,
        @DrawableRes val watchAllIIcon: Int,
        val info: String,
        val config: PlaylistDomain.PlaylistConfigDomain,
        val showDefault: Boolean
    )

    data class State constructor(
        var isCreate: Boolean = false,
        var model: Model? = null,
        var isAllWatched: Boolean? = null,
        var playlistParent: PlaylistDomain? = null,
        var defaultInitial: Boolean = false
    ) {
        lateinit var source: OrchestratorContract.Source
        lateinit var playlistEdit: PlaylistDomain
    }

    companion object {
        fun makeNav(id: Long, source: OrchestratorContract.Source) = NavigationModel(
            NavigationModel.Target.PLAYLIST_EDIT_FRAGMENT, mapOf(
                NavigationModel.Param.PLAYLIST_ID to id,
                NavigationModel.Param.SOURCE to source
            )
        )

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistEditFragment>()) {
                viewModel {
                    PlaylistEditViewModel(
                        state = get(),
                        mapper = get(),
                        playlistOrchestrator = get(),
                        mediaOrchestrator = get(),
                        log = get(),
                        imageProvider = get(),
                        prefsWrapper = get()
                    )
                }
                scoped { State() }
                factory { PlaylistEditModelMapper(res = get(), validator = get()) }
                scoped { ChipCreator((getSource() as Fragment).requireActivity(), get(), get()) }
                factory { PlaylistValidator(get()) }
                scoped { AndroidSnackbarWrapper((getSource() as Fragment).requireActivity(), get()) }
                scoped { navigationMapper(true, getSource<Fragment>().requireActivity() as AppCompatActivity) }
            }
        }
    }
}