package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_CREATE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_EDIT
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.validator.ValidatorModel
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

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
        val showDefault: Boolean,
        val isCreate: Boolean,
        var isDialog: Boolean
    )

    data class State constructor(
        var isCreate: Boolean = false,
        var model: Model? = null,
        var isAllWatched: Boolean? = null,
        var playlistParent: PlaylistDomain? = null,
        var defaultInitial: Boolean = false,
        var treeLookup: Map<Long, PlaylistTreeDomain> = mapOf(),
        var isLoaded: Boolean = false,
        var isDialog: Boolean = false,
        var transitionFinished: Boolean = false
    ) {
        fun isInitialized(): Boolean = this::playlistEdit.isInitialized

        lateinit var source: OrchestratorContract.Source
        lateinit var playlistEdit: PlaylistDomain
    }

    companion object {
        fun makeNav(id: Long, source: OrchestratorContract.Source, thumbNailUrl: String?) =
            NavigationModel(
                PLAYLIST_EDIT,
                mapOf(PLAYLIST_ID to id, SOURCE to source, IMAGE_URL to thumbNailUrl)
            )

        fun makeCreateNav(source: OrchestratorContract.Source) = NavigationModel(
            PLAYLIST_CREATE,
            mapOf(SOURCE to source)
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
                        prefsWrapper = get(),
                        recentLocalPlaylists = get(),
                        res = get()
                    )
                }
                scoped { State() }
                factory { PlaylistEditModelMapper(res = get(), validator = get()) }
                scoped { ChipCreator(this.getFragmentActivity(), get(), get()) }
                factory { PlaylistValidator(get()) }
                scoped<SnackbarWrapper> {
                    AndroidSnackbarWrapper(this.getFragmentActivity(), get())
                }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
            }
        }
    }
}