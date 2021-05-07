package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.validator.ValidatorModel
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface PlaylistEditContract {

    data class Model(
        val titleDisplay: CharSequence,
        val titleEdit: CharSequence,
        val imageUrl: String?,
        val thumbUrl: String?,
        val starred: Boolean,
        val button: String?,
        val pinned: Boolean,
        val playFromStart: Boolean,
        val default: Boolean,
        val chip: ChipModel = ChipModel.PLAYLIST_SELECT_MODEL,
        val validation: ValidatorModel?,
        @StringRes val watchAllText: Int,
        @DrawableRes val watchAllIIcon: Int
    )

    data class State constructor(
        var isCreate: Boolean = false,
        var model: Model? = null,
        var isAllWatched: Boolean? = null
    ) {
        lateinit var source: OrchestratorContract.Source

        //lateinit var playlist: PlaylistDomain
        lateinit var playlistEdit: PlaylistDomain
    }

    companion object {

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
                        prefsWrapper = get(named<GeneralPreferences>())
                    )
                }
                scoped { State() }
                factory { PlaylistEditModelMapper(res = get(), validator = get()) }
                scoped { ChipCreator((getSource() as Fragment).requireActivity(), get(), get()) }
                factory { PlaylistValidator(get()) }
                scoped { AndroidSnackbarWrapper((getSource() as Fragment).requireActivity(), get()) }
            }
        }
    }
}