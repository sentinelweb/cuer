package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.serialization.Serializable
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract.DescriptionModel
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface PlaylistItemEditContract {

    interface DoneNavigation {
        fun navigateDone()
    }

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
                        queue = get(),
                        ytContextHolder = get(),
                        toast = get(),
                        mediaOrchestrator = get(),
                        playlistItemOrchestrator = get(),
                        playlistOrchestrator = get(),
                        prefsWrapper = get(),
                        floatingService = get(),
                        shareWrapper = get()
                    )
                }
                scoped { State() }
                scoped { PlaylistItemEditModelMapper(get(), get(), get(), get()) }
                scoped { navigationMapper(true, getSource<Fragment>().requireActivity() as AppCompatActivity) }
                scoped { SelectDialogCreator((getSource() as Fragment).requireActivity()) }
                scoped { getSource<Fragment>().requireActivity() }
                scoped { AlertDialogCreator((getSource() as Fragment).requireActivity()) }
                scoped {
                    AndroidSnackbarWrapper(
                        (getSource() as Fragment).requireActivity(),
                        get()
                    )
                }
                scoped { ShareWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
            }
        }

    }
}