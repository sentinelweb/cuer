package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsAdapter
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

interface PlaylistsDialogContract {

    interface Presenter {
        fun destroy()
        fun refreshList()
        fun onItemClicked(item: ItemContract.Model)
        fun onResume()
        fun onPause()
        fun setConfig(config: Config)
        fun onAddPlaylist()
        fun onDismiss()
        fun onPinSelectedPlaylist(b: Boolean)
    }

    interface View {
        fun setList(model: Model, animate: Boolean = true)
        fun dismiss()
        fun updateDialogModel(model: Model)
    }

    data class Config(
        val selectedPlaylists: Set<PlaylistDomain>,
        val multi: Boolean,
        val itemClick: (PlaylistDomain?, Boolean) -> Unit,
        val confirm: (() -> Unit)?,
        val dismiss: () -> Unit,
        val suggestionsMedia: MediaDomain? = null,
        val showAdd: Boolean = true,
        val showPin: Boolean = true
    ) : DialogModel(Type.PLAYLIST_FULL, R.string.playlist_dialog_title)

    data class State(
        var playlists: List<PlaylistDomain> = listOf(),
        var dragFrom: Int? = null,
        var dragTo: Int? = null,
        var playlistStats: List<PlaylistStatDomain> = listOf(),
        var priorityPlaylistIds: MutableList<Long> = mutableListOf(),
        var channelSearchApplied: Boolean = false,
        var pinWhenSelected: Boolean = false,
        var playlistsModel: PlaylistsContract.Model? = null
    ) : ViewModel() {
        lateinit var config: Config
        lateinit var treeRoot: PlaylistTreeDomain
        lateinit var treeLookup: Map<Long, PlaylistTreeDomain>
    }

    data class Model(
        val playistsModel: PlaylistsContract.Model?,
        val showAdd: Boolean,
        val showPin: Boolean,
        val showUnPin: Boolean
    )

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistsDialogFragment>()) {
                scoped<View> { getSource() }
                scoped<Presenter> {
                    PlaylistsDialogPresenter(
                        view = get(),
                        state = get(),
                        playlistOrchestrator = get(),
                        playlistStatsOrchestrator = get(),
                        modelMapper = get(),
                        log = get(),
                        toastWrapper = get(),
                        prefsWrapper = get(),
                        coroutines = get(),
                        dialogModelMapper = get()
                    )
                }
                scoped { PlaylistsModelMapper(get()) }
                scoped { PlaylistsDialogModelMapper() }
                scoped { PlaylistsAdapter(get(), getSource()) }
                scoped { ItemTouchHelperCallback(getSource()) }
                scoped { ItemTouchHelper(get<ItemTouchHelperCallback>()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper((getSource() as Fragment).requireActivity(), get()) }
                scoped { ItemFactory(get()) }
                scoped { ItemModelMapper(get(), get()) }
                viewModel { State() }
            }
        }

    }
}