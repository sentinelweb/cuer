package uk.co.sentinelweb.cuer.app.ui.playlist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.roche.mdas.util.wrapper.ToastWrapper
import kotlinx.android.synthetic.main.playlist_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.common.itemlist.item.ItemModel

class PlaylistFragment :
    Fragment(R.layout.playlist_fragment),
    PlaylistContract.View,
    ItemContract.Interactions {

    private val presenter: PlaylistContract.Presenter by currentScope.inject()
    private val adapter: PlaylistAdapter by currentScope.inject()
    private val toastWrapper: ToastWrapper by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //presenter.initialise()
        playlist_list.layoutManager = LinearLayoutManager(context)
        playlist_list.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        presenter.loadList()
    }
    override fun setList(list: List<ItemModel>) {
        playlist_swipe.isRefreshing = false
        adapter.data = list
        playlist_swipe.setOnRefreshListener { presenter.loadList() }
    }

    override fun onClick(item: ItemModel) {
        toastWrapper.showToast("click: ${item.topText}")
    }

    override fun onRightSwipe(item: ItemModel) {
        toastWrapper.showToast("right: ${item.topText}")
    }

    override fun onLeftSwipe(item: ItemModel) {
        toastWrapper.showToast("left: ${item.topText}")
    }

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistFragment>()) {
                scoped<PlaylistContract.View> { getSource() }
                scoped<PlaylistContract.Presenter> { PlaylistPresenter(get(), get(), get(), get(), get()) }
                scoped { PlaylistModelMapper() }
                scoped { PlaylistAdapter(get(), getSource()) }
                viewModel { PlaylistState() }
            }
        }
    }
}
