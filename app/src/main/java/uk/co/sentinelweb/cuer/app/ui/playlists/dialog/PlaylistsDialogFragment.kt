package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.playlists_dialog_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsAdapter
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PlaylistsDialogFragment(private val config: PlaylistsDialogContract.Config) :
    DialogFragment(),
    PlaylistsDialogContract.View,
    ItemContract.Interactions,
    ItemBaseContract.ItemMoveInteractions {

    private val presenter: PlaylistsDialogContract.Presenter by currentScope.inject()
    private val adapter: PlaylistsAdapter by currentScope.inject()
    private val itemTouchHelper: ItemTouchHelper by currentScope.inject()
    private val log: LogWrapper by inject()

    init {
        log.tag(this)
        log.d("${hashCode()} - init")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        log.d("${hashCode()} - onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.playlists_dialog_fragment, parent, false)
    }

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        log.d("${hashCode()} - onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        pdf_list.layoutManager = LinearLayoutManager(context)
        pdf_list.adapter = adapter
        pdf_swipe.setOnRefreshListener { presenter.refreshList() }
        pdf_add_button.setOnClickListener { presenter.onAddPlaylist() }
        //itemTouchHelper.attachToRecyclerView(pdf_list)

        presenter.setConfig(config)
    }

    override fun onDestroyView() {
        log.d("${hashCode()} - onDestroyView")
        presenter.destroy()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        log.d("${hashCode()} - onCreateOptionsMenu")
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        log.d("${hashCode()} - onResume")
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        log.d("${hashCode()} - onPause")
        super.onPause()
        presenter.onPause()
    }
    // endregion

    // region PlaylistContract.View
    override fun setList(model: PlaylistsContract.Model, animate: Boolean) {
        pdf_swipe.isRefreshing = false
        adapter.currentPlaylistId = model.currentPlaylistId
        adapter.setData(model.items, animate)
        pdf_swipe.setOnRefreshListener { presenter.refreshList() }
    }
    //endregion

    // region ItemContract.ItemMoveInteractions
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return true
    }

    override fun onItemClear() {

    }
    //endregion

    // region ItemContract.Interactions
    override fun onClick(item: ItemContract.Model) {
        presenter.onItemClicked(item)
    }

    override fun onRightSwipe(item: ItemContract.Model) {

    }

    override fun onLeftSwipe(item: ItemContract.Model) {

    }

    override fun onPlay(item: ItemContract.Model, external: Boolean) {

    }

    override fun onStar(item: ItemContract.Model) {

    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    override fun onShare(item: ItemContract.Model) {
        //presenter.onItemShare(item)
    }
    //endregion

    companion object {

        fun newInstance(config: PlaylistsDialogContract.Config): PlaylistsDialogFragment {
            return PlaylistsDialogFragment(config)
        }

    }
}
