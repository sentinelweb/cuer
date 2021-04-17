package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Companion.PLAYLIST_SELECT_MODEL
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

interface PlaylistItemEditContract {

    interface DoneNavigation {
        fun navigateDone()
    }

    data class Model constructor(
        val imageUrl: String?,
        val title: CharSequence?,
        val description: String?,
        val chips: List<ChipModel> = listOf(PLAYLIST_SELECT_MODEL),
        val channelTitle: String?,
        val channelThumbUrl: String?,
        val channelDescription: String?,
        val pubDate: String?,
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

    data class State(
        var model: Model? = null,
        var media: MediaDomain? = null,
        val selectedPlaylists: MutableSet<PlaylistDomain> = mutableSetOf(),
        var committedItems: List<PlaylistItemDomain>? = null,
        var editingPlaylistItem: PlaylistItemDomain? = null,
        var isPlaylistsChanged: Boolean = false,
        var isMediaChanged: Boolean = false,
        var isSaved: Boolean = false
    ) {
        lateinit var source: OrchestratorContract.Source
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
                        playlistDialogModelCreator = get(),
                        log = get(),
                        queue = get(),
                        ytContextHolder = get(),
                        toast = get(),
                        mediaOrchestrator = get(),
                        playlistItemOrchestrator = get(),
                        playlistOrchestrator = get(),
                        prefsWrapper = get(named<GeneralPreferences>())
                    )
                }
                scoped { State() }
                scoped { PlaylistItemEditModelMapper(get(), get(), get(), get()) }
                scoped { navigationMapper(true, getSource<Fragment>().requireActivity() as AppCompatActivity) }
                //scoped { YoutubeJavaApiWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped {
                    ChipCreator((getSource() as Fragment).requireActivity(), get(), get())
                }
                scoped {
                    SelectDialogCreator((getSource() as Fragment).requireActivity())
                }
                scoped {
                    getSource<Fragment>().requireActivity() as DoneNavigation
                }
                scoped { AlertDialogCreator((getSource() as Fragment).requireActivity()) }
                scoped { AndroidSnackbarWrapper((getSource() as Fragment).requireActivity(), get()) }
            }
        }

    }
}