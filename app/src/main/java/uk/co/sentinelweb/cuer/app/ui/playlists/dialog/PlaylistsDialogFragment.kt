package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentPlaylistsDialogBinding
import uk.co.sentinelweb.cuer.app.ui.common.item.ItemBaseContract
import uk.co.sentinelweb.cuer.app.ui.common.ktx.bindFlow
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsItemMviContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsMviDialogContract.Label.Dismiss
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlists.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PlaylistsDialogFragment(private val config: PlaylistsMviDialogContract.Config) :
    DialogFragment(),
//    PlaylistsDialogContract.View,
    ItemContract.Interactions,
    ItemBaseContract.ItemMoveInteractions,
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<PlaylistsDialogFragment>()

    private val viewModel: PlaylistsDialogViewModel by inject()
    private val adapter: PlaylistsDialogAdapter by inject()
    private val log: LogWrapper by inject()

    private var _binding: FragmentPlaylistsDialogBinding? = null
    private val binding get() = _binding!!

    init {
        log.tag(this)
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaylistsDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    // region Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pdfList.layoutManager = LinearLayoutManager(context)
        binding.pdfList.adapter = adapter
        binding.pdfSwipe.setOnRefreshListener { viewModel.refreshList() }
        binding.pdfSwipe.isEnabled = false
        binding.pdfAddButton.setOnClickListener { viewModel.onAddPlaylist() }
        binding.pdfPinSelectedButton.setOnClickListener { viewModel.onPinSelectedPlaylist(false) }
        binding.pdfPinUnselectedButton.setOnClickListener { viewModel.onPinSelectedPlaylist(true) }
        // itemTouchHelper.attachToRecyclerView(pdf_list)
        viewModel.setConfig(config)
        bindFlow(viewModel.label, ::observeLabel)
        bindFlow(viewModel.model, ::updateDialogModel)
    }

    private fun observeLabel(label: PlaylistsMviDialogContract.Label) = when (label) {
        Dismiss -> dismiss()
    }

    private fun updateDialogModel(model: PlaylistsMviDialogContract.Model) {
        log.d("updateDialogModel: size:${model.playistsModel?.items?.size}")
        setList(model, false)
    }

    override fun onDismiss(dialog: DialogInterface) {
        viewModel.onDismiss()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }
    // endregion

    // region PlaylistContract.View
    private fun setList(model: PlaylistsMviDialogContract.Model, animate: Boolean) {
        updateDialogNoList(model)
        model.playistsModel?.items
            ?.apply { adapter.setData(this, animate) }
    }

    private fun updateDialogNoList(model: PlaylistsMviDialogContract.Model) {
        binding.pdfSwipe.isRefreshing = false
        adapter.currentPlaylistId = model.playistsModel?.currentPlaylistId
        binding.pdfAddButton.isVisible = model.showAdd
        binding.pdfButtonBg.isVisible = model.showAdd
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
    override fun onClick(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {
        viewModel.onItemClicked(item)
    }

    override fun onRightSwipe(item: PlaylistsItemMviContract.Model) {

    }

    override fun onLeftSwipe(item: PlaylistsItemMviContract.Model) {

    }

    override fun onPlay(
        item: PlaylistsItemMviContract.Model,
        external: Boolean,
        sourceView: ItemContract.ItemView
    ) {

    }

    override fun onStar(item: PlaylistsItemMviContract.Model) {

    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    override fun onShare(item: PlaylistsItemMviContract.Model) {
        //presenter.onItemShare(item)
    }

    override fun onMerge(item: PlaylistsItemMviContract.Model) {

    }

    override fun onImageClick(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {
        viewModel.onItemClicked(item)
    }

    override fun onEdit(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {

    }

    override fun onDelete(item: PlaylistsItemMviContract.Model, sourceView: ItemContract.ItemView) {

    }

    //endregion

    companion object {
        fun newInstance(config: PlaylistsMviDialogContract.Config): PlaylistsDialogFragment {
            return PlaylistsDialogFragment(config)
        }

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistsDialogFragment>()) {
                scoped {
                    PlaylistsDialogViewModel(
                        state = get(),
                        playlistOrchestrator = get(),
                        playlistStatsOrchestrator = get(),
                        modelMapper = get(),
                        log = get(),
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
                scoped { PlaylistsMviDialogContract.State() }
                scoped { PlaylistsMviDialogContract.Strings() }
            }
        }
    }
}
