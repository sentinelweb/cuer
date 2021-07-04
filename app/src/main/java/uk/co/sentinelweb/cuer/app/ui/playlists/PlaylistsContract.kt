package uk.co.sentinelweb.cuer.app.ui.playlists

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

interface PlaylistsContract {

    interface Presenter {
        fun refreshList()
        fun setFocusMedia(mediaDomain: MediaDomain)
        fun onItemSwipeRight(item: ItemContract.Model)
        fun onItemSwipeLeft(item: ItemContract.Model)
        fun onItemClicked(item: ItemContract.Model)
        fun onItemPlay(item: ItemContract.Model, external: Boolean)
        fun onItemStar(item: ItemContract.Model)
        fun onItemShare(item: ItemContract.Model)
        fun onMerge(item: ItemContract.Model)
        fun moveItem(fromPosition: Int, toPosition: Int)
        fun undoDelete()
        fun commitMove()
        fun onResume(parentId: Long?)
        fun onPause()
        fun onItemImageClicked(item: ItemContract.Model)
        fun onUpClicked()
        fun onEdit(item: ItemContract.Model)
    }

    interface View {
        fun setList(model: Model, animate: Boolean = true)
        fun scrollToItem(index: Int)
        fun hideRefresh()
        fun showUndo(msg: String, undo: () -> Unit)
        fun showMessage(msg: String)
        fun showPlaylistSelector(model: PlaylistsDialogContract.Config)
        fun navigate(nav: NavigationModel)
    }

    data class State constructor(
        var playlists: List<PlaylistDomain> = listOf(),
        var deletedPlaylist: PlaylistDomain? = null,
        var dragFrom: Int? = null,
        var dragTo: Int? = null,
        var playlistStats: List<PlaylistStatDomain> = listOf(),
        var treeRoot: PlaylistTreeDomain = PlaylistTreeDomain(),
        var treeCurrentNodeId: Long? = null,
        var playlistsDisplay: List<PlaylistDomain> = listOf(),
        var treeLookup: Map<Long, PlaylistTreeDomain> = mapOf()
    ) : ViewModel()

    data class Model(
        val title: String,
        val imageUrl: String = "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg",
        val currentPlaylistId: Identifier<*>?, // todo non null?
        val showUp: Boolean,
        val items: List<ItemContract.Model>
    )

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistsFragment>()) {
                scoped<View> { getSource() }
                scoped<Presenter> {
                    PlaylistsPresenter(
                        view = get(),
                        state = get(),
                        playlistOrchestrator = get(),
                        playlistStatsOrchestrator = get(),
                        modelMapper = get(),
                        queue = get(),
                        log = get(),
                        toastWrapper = get(),
                        prefsWrapper = get(),
                        coroutines = get(),
                        newMedia = get(),
                        recentItems = get(),
                        localSearch = get(),
                        remoteSearch = get(),
                        ytJavaApi = get(),
                        searchMapper = get(),
                        merge = get(),
                        shareWrapper = get()
                    )
                }
                scoped { PlaylistsModelMapper(get()) }
                scoped { PlaylistsAdapter(get(), getSource()) }
                scoped {
                    ItemTouchHelperCallback(
                        getSource()
                    )
                }
                scoped { ItemTouchHelper(get<ItemTouchHelperCallback>()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper((getSource() as Fragment).requireActivity(), get()) }
                scoped { YoutubeJavaApiWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ShareWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ItemFactory(get()) }
                scoped { ItemModelMapper(get(), get()) }
                scoped { navigationMapper(true, getSource<Fragment>().requireActivity() as AppCompatActivity) }
                viewModel { State() }
            }
        }

    }
}