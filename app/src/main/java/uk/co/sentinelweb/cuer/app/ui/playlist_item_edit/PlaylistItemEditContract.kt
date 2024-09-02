package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.annotation.ColorRes
import kotlinx.serialization.Serializable
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.play.PlayDialog
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract.DescriptionModel
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemTextMapper
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.PlatformLaunchWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface PlaylistItemEditContract {

    data class Model constructor(
        val description: DescriptionModel,
        val imageUrl: String?,
        val durationText: String?,
        val positionText: String?,
        val position: Float?,
        val starred: Boolean,
        val canPlay: Boolean,
        val empty: Boolean,
        val isLive: Boolean,
        val isUpcoming: Boolean,
        @ColorRes val infoTextColor: Int,
        val isPlayEnabled: Boolean,
        val itemText: CharSequence,
    )

    @Serializable
    data class State(
        @kotlinx.serialization.Transient
        var model: Model? = null,
        var media: MediaDomain? = null,
        val selectedPlaylists: MutableSet<PlaylistDomain> = mutableSetOf(),
        var committedItems: List<PlaylistItemDomain>? = null,
        var editingPlaylistItem: PlaylistItemDomain? = null,
        var isPlaylistsChanged: Boolean = false,
        var isMediaChanged: Boolean = false,
        var isSaved: Boolean = false,
        val editSettings: Edit = Edit(),
        var parentPlaylistId: OrchestratorContract.Identifier<GUID>? = null,
        var allowPlay: Boolean = false,
        val deletedPlayLists: MutableSet<PlaylistDomain> = mutableSetOf(),
        var isOnSharePlaylist: Boolean = false,
        var isInShare: Boolean = false
    ) {

        lateinit var source: OrchestratorContract.Source

        @Serializable
        data class Edit constructor(
            var watched: Boolean? = null,
            var playFromStart: Boolean? = null
        )
    }

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistItemEditFragment>()) {
                viewModel {
                    PlaylistItemEditViewModel(
                        state = get(),
                        modelMapper = get(),
                        itemCreator = get(),
                        log = get(),
                        toast = get(),
                        mediaOrchestrator = get(),
                        playlistItemOrchestrator = get(),
                        playlistOrchestrator = get(),
                        prefsWrapper = get(),
                        shareWrapper = get(),
                        playUseCase = get(),
                        linkNavigator = get(),
                        recentLocalPlaylists = get(),
                        res = get(),
                        coroutines = get(),
                        timeProvider = get(),
                        paiMapper = get(),
                        mediaUpdateFromPlatformUseCase = get()
                    )
                }
                scoped { State() }
                scoped { PlaylistItemEditModelMapper(get(), get(), get(), get(), get(), get(), get()) }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                scoped { SelectDialogCreator(this.getFragmentActivity()) }
                scoped { this.getFragmentActivity() }
                scoped { AndroidSnackbarWrapper(this.getFragmentActivity(), get()) }
                scoped { AndroidShareWrapper(this.getFragmentActivity()) }
                scoped<PlatformLaunchWrapper> { YoutubeJavaApiWrapper(this.getFragmentActivity(), get()) }
                scoped {
                    PlayUseCase(
                        queue = get(),
                        ytCastContextHolder = get(),
                        prefsWrapper = get(),
                        coroutines = get(),
                        floatingService = get(),
                        strings = get(),
                        cuerCastPlayerWatcher = get(),
                        alertDialogCreator = get(),
                        parentScope = get<PlaylistItemEditFragment>().scope
                    )
                }
                factory<PlayUseCase.Dialog> {
                    PlayDialog(
                        get<PlaylistItemEditFragment>(),
                        itemFactory = get(),
                        itemModelMapper = get(),
                        navigationRouter = get(),
                        castDialogWrapper = get(),
                        floatingService = get(),
                        log = get(),
                        youtubeApi = get(),
                    )
                }
                scoped { ItemFactory(get(), get(), get()) }
                scoped { ItemModelMapper(get(), get(), get(), get()) }
                scoped { ItemTextMapper(get(), get()) }
                scoped { PlaylistItemEditHelpConfig(get()) }
            }
        }

    }
}
