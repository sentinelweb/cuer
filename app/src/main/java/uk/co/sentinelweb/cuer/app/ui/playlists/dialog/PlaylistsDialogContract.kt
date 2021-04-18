package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import org.koin.android.viewmodel.dsl.viewModel
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
    }

    interface View {
        fun setList(model: PlaylistsContract.Model, animate: Boolean = true)
        fun dismiss()
    }

    data class Config(
        val selectedPlaylists: Set<PlaylistDomain>,
        val multi: Boolean,
        val itemClick: (PlaylistDomain?, Boolean) -> Unit,
        val confirm: (() -> Unit)?,
        val dismiss: () -> Unit,
        val suggestionsMedia: MediaDomain? = null,
        val showAdd: Boolean = true
    ) : DialogModel(Type.PLAYLIST_FULL, R.string.playlist_dialog_title)

    data class State(
        var playlists: List<PlaylistDomain> = listOf(),
        var dragFrom: Int? = null,
        var dragTo: Int? = null,
        var playlistStats: List<PlaylistStatDomain> = listOf(),
        var priorityPlaylistIds: MutableList<Long> = mutableListOf(),
        var channelSearchApplied: Boolean = false
    ) : ViewModel() {
        lateinit var config: Config
    }

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
                        playlistRepository = get(),
                        modelMapper = get(),
                        log = get(),
                        toastWrapper = get(),
                        prefsWrapper = get(named<GeneralPreferences>()),
                        coroutines = get()
                    )
                }
                scoped { PlaylistsModelMapper() }
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