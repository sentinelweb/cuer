package uk.co.sentinelweb.cuer.app.ui.playlists

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
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
        fun performMove(item: PlaylistsItemMviContract.Model)
        fun performDelete(item: PlaylistsItemMviContract.Model)
        fun performOpen(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView)
        fun performPlay(
            item: PlaylistsItemMviContract.Model,
            external: Boolean,
            sourceView: ItemContract.ItemView
        )

        fun performStar(item: PlaylistsItemMviContract.Model)
        fun performShare(item: PlaylistsItemMviContract.Model)
        fun performMerge(item: PlaylistsItemMviContract.Model)
        fun moveItem(fromPosition: Int, toPosition: Int)
        fun undoDelete()
        fun commitMove()
        fun onResume(parentId: Long?)
        fun onPause()
        fun onItemImageClicked(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView)
        fun performEdit(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView)
        fun onCreatePlaylist()
    }

    interface View {
        fun setList(model: Model, animate: Boolean = true)
        fun scrollToItem(index: Int)
        fun hideRefresh()
        fun showUndo(msg: String, undo: () -> Unit)
        fun showMessage(msg: String)
        fun showError(msg: String)
        fun showPlaylistSelector(model: PlaylistsMviDialogContract.Config)
        fun repaint()
        fun navigate(nav: NavigationModel, sourceView: ItemContract.ItemView?)
        fun notifyItemRemoved(model: PlaylistsItemMviContract.Model)
    }

    data class State constructor(
        var playlists: List<PlaylistDomain> = listOf(),
        var deletedPlaylist: PlaylistDomain? = null,
        var dragFrom: Int? = null,
        var dragTo: Int? = null,
        var playlistStats: List<PlaylistStatDomain> = listOf(),
        var treeRoot: PlaylistTreeDomain = PlaylistTreeDomain(),
        var treeLookup: Map<Long, PlaylistTreeDomain> = mapOf()
    ) : ViewModel()

    data class Model(
        val title: String,
        val imageUrl: String = "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg",
        val currentPlaylistId: Identifier<*>?, // todo non null?
        val items: List<PlaylistsItemMviContract.Model>
    )

    companion object {
        val PLAYLIST_TRANS_IMAGE by lazy {
            GlobalContext.get().get<ResourceWrapper>().getString(R.string.playlist_trans_image)
        }
        val PLAYLIST_TRANS_TITLE by lazy {
            GlobalContext.get().get<ResourceWrapper>().getString(R.string.playlist_trans_title)
        }

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistsFragment>()) {
                scoped<View> { get<PlaylistsFragment>() }
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
                        shareWrapper = get(),
                        recentLocalPlaylists = get(),
                        starredItems = get(),
                        unfinishedItems = get(),
                        res = get(),
                    )
                }
                scoped { PlaylistsModelMapper(get()) }
                scoped<SnackbarWrapper> {
                    AndroidSnackbarWrapper(
                        this.getFragmentActivity(),
                        get()
                    )
                }
                scoped { YoutubeJavaApiWrapper(this.getFragmentActivity(), get()) }
                scoped { AndroidShareWrapper(this.getFragmentActivity()) }
                scoped { ItemFactory(get()) }
                scoped { ItemModelMapper(get(), get()) }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                viewModel { State() }
                scoped { PlaylistsHelpConfig(get()) }
            }
        }
    }
}