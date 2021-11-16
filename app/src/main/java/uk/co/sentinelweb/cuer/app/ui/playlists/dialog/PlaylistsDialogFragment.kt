package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.databinding.PlaylistsDialogFragmentBinding
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PlaylistsDialogFragment(private val config: PlaylistsDialogContract.Config) :
    DialogFragment(),
    PlaylistsDialogContract.View,
    ItemContract.Interactions,
    ItemBaseContract.ItemMoveInteractions,
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val presenter: PlaylistsDialogContract.Presenter by inject()
    private val adapter: PlaylistsDialogAdapter by inject()
    private val itemTouchHelper: ItemTouchHelper by inject()
    private val log: LogWrapper by inject()

    private var _binding: PlaylistsDialogFragmentBinding? = null
    private val binding get() = _binding!!

    init {
        log.tag(this)
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = PlaylistsDialogFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pdfList.layoutManager = LinearLayoutManager(context)
        binding.pdfList.adapter = adapter
        binding.pdfSwipe.setOnRefreshListener { presenter.refreshList() }
        binding.pdfAddButton.setOnClickListener { presenter.onAddPlaylist() }
        binding.pdfPinSelectedButton.setOnClickListener { presenter.onPinSelectedPlaylist(false) }
        binding.pdfPinUnselectedButton.setOnClickListener { presenter.onPinSelectedPlaylist(true) }
        //itemTouchHelper.attachToRecyclerView(pdf_list)

        presenter.setConfig(config)
    }

    override fun updateDialogModel(model: PlaylistsDialogContract.Model) {
        updateDialogNoList(model)
    }

    override fun onDismiss(dialog: DialogInterface) {
        presenter.onDismiss()
    }

    override fun onDestroyView() {
        presenter.destroy()
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }
    // endregion

    // region PlaylistContract.View
    override fun setList(model: PlaylistsDialogContract.Model, animate: Boolean) {
        updateDialogNoList(model)
        model.playistsModel?.items?.apply { adapter.setData(this, animate) }
    }

    private fun updateDialogNoList(model: PlaylistsDialogContract.Model) {
        binding.pdfSwipe.isRefreshing = false
        adapter.currentPlaylistId = model.playistsModel?.currentPlaylistId
        binding.pdfAddButton.isVisible = model.showAdd
        binding.pdfPinSelectedButton.isVisible = model.showPin
        binding.pdfPinUnselectedButton.isVisible = model.showUnPin
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
    override fun onClick(item: ItemContract.Model, sourceView: ItemContract.ItemView) {
        presenter.onItemClicked(item)
    }

    override fun onRightSwipe(item: ItemContract.Model) {

    }

    override fun onLeftSwipe(item: ItemContract.Model) {

    }

    override fun onPlay(
        item: ItemContract.Model,
        external: Boolean,
        sourceView: ItemContract.ItemView
    ) {

    }

    override fun onStar(item: ItemContract.Model) {

    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    override fun onShare(item: ItemContract.Model) {
        //presenter.onItemShare(item)
    }

    override fun onMerge(item: ItemContract.Model) {

    }

    override fun onImageClick(item: ItemContract.Model, sourceView: ItemContract.ItemView) {
        presenter.onItemClicked(item)
    }

    override fun onEdit(item: ItemContract.Model, sourceView: ItemContract.ItemView) {

    }

    //endregion

    companion object {

        fun newInstance(config: PlaylistsDialogContract.Config): PlaylistsDialogFragment {
            return PlaylistsDialogFragment(config)
        }

    }
}
