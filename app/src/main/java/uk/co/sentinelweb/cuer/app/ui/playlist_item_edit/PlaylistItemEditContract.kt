package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.annotation.ColorRes
import kotlinx.serialization.Serializable
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.play.PlayDialog
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract.DescriptionModel
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
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
        @ColorRes val infoTextBackgroundColor: Int
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
        var parentPlaylistId: Long = -1,
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
                    )
                }
                scoped { State() }
                scoped { PlaylistItemEditModelMapper(get(), get(), get(), get()) }
                scoped { navigationMapper(true, this.getFragmentActivity()) }
                scoped { SelectDialogCreator(this.getFragmentActivity()) }
                scoped { this.getFragmentActivity() }
                scoped { AlertDialogCreator(this.getFragmentActivity()) }
                scoped { AndroidSnackbarWrapper(this.getFragmentActivity(), get()) }
                scoped { ShareWrapper(this.getFragmentActivity()) }
                scoped { YoutubeJavaApiWrapper(this.getFragmentActivity()) }
                scoped {
                    PlayUseCase(
                        queue = get(),
                        ytCastContextHolder = get(),
                        prefsWrapper = get(),
                        coroutines = get(),
                        floatingService = get(),
                        playDialog = get(),
                    )
                }
                scoped {
                    PlayDialog(
                        getSource(),
                        itemFactory = get(),
                        itemModelMapper = get(),
                        navigationMapper = get(),
                        castDialogWrapper = get(),
                        floatingService = get(),
                        log = get(),
                        alertDialogCreator = get(),
                        youtubeApi = get(),
                    )
                }
                scoped { ItemFactory(get(), get(), get()) }
                scoped { ItemModelMapper(get(), get(), get(), get()) }
            }
        }

    }
}