package uk.co.sentinelweb.cuer.app.ui.playlists

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemTouchHelperCallback
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistStatDomain

interface PlaylistsContract {

    interface Presenter {
        fun initialise()
        fun destroy()
        fun refreshList()
        fun setFocusMedia(mediaDomain: MediaDomain)
        fun onItemSwipeRight(item: ItemContract.Model)
        fun onItemSwipeLeft(item: ItemContract.Model)
        fun onItemClicked(item: ItemContract.Model)
        fun onItemPlay(item: ItemContract.Model, external: Boolean)
        fun onItemStar(item: ItemContract.Model)
        fun onItemShare(item: ItemContract.Model)
        fun moveItem(fromPosition: Int, toPosition: Int)

        //fun scroll(direction: ScrollDirection)
        fun undoDelete()
        fun commitMove()
        fun onResume()
        fun onPause()
    }

    interface View {
        fun setList(model: Model, animate: Boolean = true)
        fun scrollToItem(index: Int)

        //fun scrollTo(direction: ScrollDirection)
        fun showDeleteUndo(msg: String)
        fun gotoPlaylist(id: Long, play: Boolean)
        fun gotoEdit(id: Long)
    }

    enum class ScrollDirection { Up, Down, Top, Bottom }

    data class State constructor(
        var playlists: List<PlaylistDomain> = listOf(),
        var deletedPlaylist: PlaylistDomain? = null,
        var dragFrom: Int? = null,
        var dragTo: Int? = null,
        var playlistStats: List<PlaylistStatDomain> = listOf()
    ) : ViewModel()

    data class Model(
        val imageUrl: String = "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg",
        val currentPlaylistId: Identifier<*>?, // todo non null?
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
                        playlistRepository = get(),
                        modelMapper = get(),
                        queue = get(),
                        log = get(),
                        toastWrapper = get(),
                        prefsWrapper = get(named<GeneralPreferences>()),
                        coroutines = get()
                    )
                }
                scoped { PlaylistsModelMapper() }
                scoped { PlaylistsAdapter(get(), getSource()) }
                scoped {
                    ItemTouchHelperCallback(
                        getSource()
                    )
                }
                scoped { ItemTouchHelper(get<ItemTouchHelperCallback>()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper((getSource() as Fragment).requireActivity()) }
                scoped { YoutubeJavaApiWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ShareWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped { ItemFactory(get()) }
                scoped { ItemModelMapper(get(), get()) }
                viewModel { State() }
            }
        }

    }
}