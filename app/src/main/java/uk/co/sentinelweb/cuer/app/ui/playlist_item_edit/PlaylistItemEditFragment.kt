package uk.co.sentinelweb.cuer.app.ui.playlist_item_edit

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.transition.TransitionInflater
import android.view.*
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.playlist_item_edit_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.core.context.KoinContextHandler
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT
import uk.co.sentinelweb.cuer.app.ui.common.dialog.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.SOURCE
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.NAV_DONE
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditViewModel.UiEvent.Type.ERROR
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditViewModel.UiEvent.Type.REFRESHING
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogContract
import uk.co.sentinelweb.cuer.app.ui.playlists.dialog.PlaylistsDialogFragment
import uk.co.sentinelweb.cuer.app.ui.share.ShareContract
import uk.co.sentinelweb.cuer.app.util.cast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.glide.GlideFallbackLoadListener
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistItem

class PlaylistItemEditFragment
    : Fragment(R.layout.playlist_item_edit_fragment),
    ShareContract.Committer {

    private val viewModel: PlaylistItemEditViewModel by currentScope.inject()
    private val log: LogWrapper by inject()
    private val navMapper: NavigationMapper by currentScope.inject()
    private val chipCreator: ChipCreator by currentScope.inject()
    private val selectDialogCreator: SelectDialogCreator by currentScope.inject()
    private val res: ResourceWrapper by currentScope.inject()
    private val castDialogWrapper: CastDialogWrapper by inject()
    private val alertDialogCreator: AlertDialogCreator by currentScope.inject()
    private val doneNavigation: PlaylistItemEditContract.DoneNavigation by currentScope.inject()// from activity (see onAttach)
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()

    private val starMenuItem: MenuItem
        get() = ple_toolbar.menu.findItem(R.id.plie_star)
    private val playMenuItem: MenuItem
        get() = ple_toolbar.menu.findItem(R.id.plie_play)

    private var dialog: AppCompatDialog? = null
    private var dialogFragment: DialogFragment? = null

    // todo extract
    private val ytDrawable: Drawable by lazy {
        res.getDrawable(R.drawable.ic_platform_youtube_24_black, R.color.primary)
    }

    private object menuState {
        var modelEmpty = false
        var scrolledDown = false
    }

    private val itemArg: PlaylistItemDomain? by lazy {
        PLAYLIST_ITEM.getString(arguments)?.let { deserialisePlaylistItem(it) }
    }

    private val sourceArg: Source by lazy {
        SOURCE.getEnum<Source>(arguments) ?: Source.LOCAL
    }

    init {
        log.tag(this)
    }

    // saves the data on back press (enabled in onResume)
    private val saveCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.checkToSave()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        itemArg?.id?.apply {
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ple_toolbar.let {
            (activity as AppCompatActivity).setSupportActionBar(it)
        }
        //ple_play_button.setOnClickListener { viewModel.onPlayVideoLocal() }
        ple_star_fab.setOnClickListener { viewModel.onStarClick() }
        ple_play_fab.setOnClickListener { viewModel.onPlayVideo() }
        starMenuItem.isVisible = false
        playMenuItem.isVisible = false
        ple_author_image.setOnClickListener { viewModel.onChannelClick() }
        ple_desc.setMovementMethod(object : LinkMovementMethod() {
            override fun handleMovementKey(
                widget: TextView?,
                buffer: Spannable?,
                keyCode: Int,
                movementMetaState: Int,
                event: KeyEvent?
            ): Boolean {
                buffer?.run { viewModel.onLinkClick(this.toString()) }
                return true
            }
        })
        ple_toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.plie_star -> {
                    viewModel.onStarClick()
                    true
                }
                R.id.plie_play -> {
                    viewModel.onPlayVideo()
                    true
                }
                else -> false
            }
        }
        ple_swipe.setOnRefreshListener { viewModel.refreshMediaBackground() }
        ple_appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {

            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange()
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    // only show the menu items for the non-empty state
                    starMenuItem.isVisible = !menuState.modelEmpty
                    playMenuItem.isVisible = !menuState.modelEmpty
                } else if (isShow) {
                    isShow = false
                    starMenuItem.isVisible = false
                    playMenuItem.isVisible = false
                }
                menuState.scrolledDown = isShow
            }
        })

        // setup data for fragment transition
        itemArg?.apply {
            saveCallback.isEnabled = true
            if (id != null) { // fixme needs new flag assumes transition
                media.image?.apply {
                    Glide.with(requireContext())
                        .load(this.url)
                        .into(ple_image)
                }
                //ple_play_button.isVisible = false
                ple_author_image.isVisible = false
                ple_title_pos.isVisible = false
                ple_title_bg.isVisible = false
                ple_star_fab.isVisible = false
                starMenuItem.isVisible = false
                playMenuItem.isVisible = false


                viewModel.delayedSetData(this, sourceArg)
            } else {
                viewModel.setData(this, sourceArg)
            }
        }
        observeUi()
        observeModel()
        observeNavigation()
        observeDialog()
    }

    private fun observeUi() {
        viewModel.getUiObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistItemEditViewModel.UiEvent> {
                override fun onChanged(model: PlaylistItemEditViewModel.UiEvent) {
                    when (model.type) {
                        REFRESHING -> ple_swipe.isRefreshing = model.data as Boolean
                        ERROR -> snackbarWrapper.makeError(model.data as String).show()
                    }
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.playlist_item_edit_actionbar, menu)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, saveCallback)
        currentScope.linkTo(requireActivity().currentScope)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /* init */ viewModel
    }

    private fun observeModel() {
        viewModel.getModelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistItemEditContract.Model> {
                override fun onChanged(model: PlaylistItemEditContract.Model) {
                    ple_title_bg.isVisible = true
                    ple_author_image.isVisible = !model.empty
                    ple_title_pos.isVisible = !model.empty
                    ple_duration.isVisible = !model.empty
                    ple_star_fab.isVisible = !model.empty
                    if (menuState.scrolledDown) {
                        starMenuItem.isVisible = !model.empty
                        playMenuItem.isVisible = !model.empty
                    } else {
                        starMenuItem.isVisible = false
                        playMenuItem.isVisible = false
                    }
                    Glide.with(requireContext())
                        .load(model.imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ple_image)
                    ple_desc.text = model.description
                    ple_title.text = model.title
                    ple_collapsing_toolbar.title = model.title
                    ple_toolbar.title = model.title
                    menuState.modelEmpty = model.empty
                    ple_swipe.isRefreshing = false
                    if (model.empty) {
                        return
                    }

                    model.channelThumbUrl?.also { url ->
                        Glide.with(requireContext())
                            .load(url)
                            .circleCrop()
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .addListener(GlideFallbackLoadListener(ple_author_image, url, ytDrawable, log))
                            .into(ple_author_image)
                        //.onLoadFailed(ytDrawable)
                    } ?: run { ple_author_image.setImageDrawable(ytDrawable) }

                    ple_chips.removeAllViews()
                    model.chips.forEach { chipModel ->
                        chipCreator.create(chipModel, ple_chips)?.apply {
                            ple_chips.addView(this)
                            when (chipModel.type) {
                                PLAYLIST_SELECT -> {
                                    setOnClickListener { viewModel.onSelectPlaylistChipClick(chipModel) }
                                }
                                PLAYLIST -> {
                                    setOnCloseIconClickListener { viewModel.onRemovePlaylist(chipModel) }
                                }
                            }
                        }
                    }
                    ple_pub_date.text = model.pubDate
                    ple_duration.text = model.durationText
                    ple_duration.setBackgroundColor(res.getColor(model.infoTextBackgroundColor))

                    model.position?.let { ratio ->
                        ple_title_pos.layoutParams.width = (ratio * ple_title_bg.width).toInt()
                    } ?: ple_title_pos.apply { isVisible = false }
                    ple_pub_date.text = model.pubDate
                    ple_author_title.text = model.channelTitle
                    ple_author_desc.text = model.channelDescription
                    val starIconResource =
                        if (model.starred) R.drawable.ic_button_starred_white
                        else R.drawable.ic_button_unstarred_white
                    starMenuItem.setIcon(starIconResource)
                    ple_star_fab.setImageResource(starIconResource)
                    ple_star_fab.setImageResource(starIconResource)
                }
            })
    }

    private fun observeNavigation() {
        viewModel.getNavigationObservable().observe(this.viewLifecycleOwner,
            object : Observer<NavigationModel> {
                override fun onChanged(nav: NavigationModel) {
                    when (nav.target) {
                        NAV_DONE -> doneNavigation.navigateDone()//navigateDone()
                        else -> navMapper.map(nav)
                    }
                }
            }
        )
    }

    private fun observeDialog() {
        viewModel.getDialogObservable().observe(this.viewLifecycleOwner,
            object : Observer<DialogModel> {
                override fun onChanged(model: DialogModel) {
                    dialog?.dismiss()
                    dialogFragment?.let {
                        val ft = childFragmentManager.beginTransaction()
                        ft.hide(it)
                        ft.commit()
                    }
                    when (model.type) {
                        DialogModel.Type.PLAYLIST_FULL -> {
                            dialogFragment =
                                PlaylistsDialogFragment.newInstance(model as PlaylistsDialogContract.Config)
                            dialogFragment?.show(childFragmentManager, SELECT_PLAYLIST_TAG)
                        }
                        DialogModel.Type.PLAYLIST_ADD -> {
                            dialogFragment = PlaylistEditFragment.newInstance()
                                .apply {
                                    listener = object : PlaylistEditFragment.Listener {
                                        override fun onPlaylistCommit(domain: PlaylistDomain?) {
                                            domain?.apply { viewModel.onPlaylistSelected(this) }
                                            dialogFragment?.dismissAllowingStateLoss()
                                        }
                                    }
                                }
                            dialogFragment?.show(childFragmentManager, CREATE_PLAYLIST_TAG)
                        }
                        DialogModel.Type.PLAYLIST -> {
                            selectDialogCreator
                                .createMulti(model as SelectDialogModel)
                                .apply { show() }
                        }
                        DialogModel.Type.SELECT_ROUTE -> {
                            castDialogWrapper.showRouteSelector(childFragmentManager)
                        }
                        DialogModel.Type.CONFIRM -> {
                            alertDialogCreator.create(model as AlertDialogModel).show()
                        }
                        else -> Unit
                    }
                }
            }
        )
    }

    override suspend fun commit(onCommit: ShareContract.Committer.OnCommit) {
        viewModel.commitPlaylistItems(onCommit)
    }

    companion object {
        private val CREATE_PLAYLIST_TAG = "pe_dialog"
        private val SELECT_PLAYLIST_TAG = "pdf_dialog"

        val TRANS_IMAGE by lazy { KoinContextHandler.get().get<ResourceWrapper>().getString(R.string.playlist_item_trans_image) }

        val TRANS_TITLE by lazy { KoinContextHandler.get().get<ResourceWrapper>().getString(R.string.playlist_item_trans_title) }
    }
}
