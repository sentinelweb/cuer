package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

interface PlaylistsDialogContract {

    interface Presenter {
        fun destroy()
        fun refreshList()
        fun onItemClicked(item: PlaylistsItemMviContract.Model)
        fun onResume()
        fun onPause()
        fun setConfig(config: PlaylistsMviDialogContract.Config)
        fun onAddPlaylist()
        fun onDismiss()
        fun onPinSelectedPlaylist(b: Boolean)
    }

    interface View {
        fun setList(model: Model, animate: Boolean = true)
        fun dismiss()
        fun updateDialogModel(model: Model)
    }

    data class State(
        var playlists: List<PlaylistDomain> = listOf(),
        var dragFrom: Int? = null,
        var dragTo: Int? = null,
        var playlistStats: List<PlaylistStatDomain> = listOf(),
        var channelPlaylistIds: MutableList<Long> = mutableListOf(),
        var pinWhenSelected: Boolean = false,
        var playlistsModel: PlaylistsContract.Model? = null
    ) : ViewModel() {
        lateinit var config: PlaylistsMviDialogContract.Config
        lateinit var treeRoot: PlaylistTreeDomain
    }

    data class Model(
        val playistsModel: PlaylistsContract.Model?,
        val showAdd: Boolean,
        val showPin: Boolean,
        val showUnPin: Boolean
    )

    companion object {
        val ADD_PLAYLIST_DUMMY = PlaylistDomain.createDummy("Add Playlist")
        val ROOT_PLAYLIST_DUMMY = PlaylistDomain.createDummy("Top level")

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistsDialogFragment>()) {
                scoped<View> { get<PlaylistsDialogFragment>() }
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
                        dialogModelMapper = get(),
                        recentLocalPlaylists = get()
                    )
                }
                scoped { PlaylistsModelMapper(get()) }
                scoped { PlaylistsDialogModelMapper() }
                scoped { PlaylistsDialogAdapter(get(), get()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper(this.getFragmentActivity(), get()) }
                scoped { ItemFactory(get()) }
                scoped { ItemModelMapper(get(), get()) }
                viewModel { State() }
            }
        }
    }
}