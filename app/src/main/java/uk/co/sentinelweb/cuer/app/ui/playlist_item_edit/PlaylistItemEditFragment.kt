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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.playlist_item_edit_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.KoinContextHandler
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipCreator
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST_SELECT
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.util.cast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.glide.GlideFallbackLoadListener
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistItem


class PlaylistItemEditFragment : Fragment(R.layout.playlist_item_edit_fragment) {

    private val viewModel: PlaylistItemEditViewModel by currentScope.inject()
    private val log: LogWrapper by inject()
    private val navMapper: NavigationMapper by currentScope.inject()
    private val chipCreator: ChipCreator by currentScope.inject()
    private val selectDialogCreator: SelectDialogCreator by currentScope.inject()
    private val res: ResourceWrapper by currentScope.inject()
    private val castDialogWrapper: CastDialogWrapper by inject()

    private val starMenuItem: MenuItem
        get() = ple_toolbar.menu.findItem(R.id.plie_star)
    private val playMenuItem: MenuItem
        get() = ple_toolbar.menu.findItem(R.id.plie_play)

    private var dialog: AppCompatDialog? = null
    private var createPlaylistDialog: DialogFragment? = null

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

    init {
        log.tag(this)
    }

    // saves the data on back press (enabled in onResume)
    private val saveCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.checkToSave()
        }
    }

    fun setData(media: MediaDomain?) = viewModel.setData(media)
    //fun setData(item: PlaylistItemDomain) = viewModel.setData(item)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (itemArg != null) {
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
        itemArg?.let { item ->
            Glide.with(requireContext())
                .load(item.media.image?.url)
                .into(ple_image)

            //ple_play_button.isVisible = false
            ple_author_image.isVisible = false
            ple_title_pos.isVisible = false
            ple_title_bg.isVisible = false
            ple_star_fab.isVisible = false
            starMenuItem.isVisible = false
            playMenuItem.isVisible = false
            saveCallback.isEnabled = true

            viewModel.delayedLoad(item)
        }
        observeModel()
        observeNavigation()
        observeDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.playlist_item_edit_actionbar, menu)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, saveCallback)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /* init */ viewModel
    }

    private fun observeModel() {
        viewModel.getModelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<PlaylistItemEditModel> {
                override fun onChanged(model: PlaylistItemEditModel) {
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
                    navMapper.map(nav)
                }
            }
        )
    }

    private fun observeDialog() {
        viewModel.getDialogObservable().observe(this.viewLifecycleOwner,
            object : Observer<DialogModel> {
                override fun onChanged(model: DialogModel) {
                    dialog?.dismiss()
                    createPlaylistDialog?.let {
                        val ft = childFragmentManager.beginTransaction()
                        ft.hide(it)
                        ft.commit()
                    }
                    when (model.type) {
                        DialogModel.Type.PLAYLIST ->
                            dialog = selectDialogCreator
                                .createMulti(model as SelectDialogModel)
                                .apply { show() }
                        DialogModel.Type.PLAYLIST_ADD -> {
                            createPlaylistDialog = PlaylistEditFragment.newInstance(null)
                                .apply {
                                    listener = object : PlaylistEditFragment.Listener {
                                        override fun onPlaylistCommit(domain: PlaylistDomain?) {
                                            domain?.apply { viewModel.onPlaylistSelected(this) }
                                            createPlaylistDialog?.dismissAllowingStateLoss()
                                        }
                                    }
                                }
                            createPlaylistDialog?.show(childFragmentManager, CREATE_PLAYLIST_TAG)
                        }
                        DialogModel.Type.SELECT_ROUTE -> {
                            castDialogWrapper.showRouteSelector(childFragmentManager)
                        }
                        else -> Unit
                    }
                }
            }
        )
    }

    suspend fun commitPlaylistItems() {
        viewModel.commitPlaylistItems()
    }

    fun getPlaylistItems() = viewModel.getCommittedItems()

    companion object {

        private val CREATE_PLAYLIST_TAG = "pe_dialog"
        val TRANS_IMAGE by lazy { KoinContextHandler.get().get<ResourceWrapper>().getString(R.string.playlist_item_trans_image) }
        val TRANS_TITLE by lazy { KoinContextHandler.get().get<ResourceWrapper>().getString(R.string.playlist_item_trans_title) }

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistItemEditFragment>()) {
                viewModel {
                    PlaylistItemEditViewModel(
                        state = get(),
                        modelMapper = get(),
                        playlistRepo = get(),
                        mediaRepo = get(),
                        itemCreator = get(),
                        playlistDialogModelCreator = get(),
                        log = get(),
                        ytInteractor = get(),
                        queue = get(),
                        ytContextHolder = get(),
                        toast = get()
                    )
                }
                scoped { PlaylistItemEditState() }
                scoped { PlaylistItemEditModelMapper(get(), get()) }
                scoped {
                    NavigationMapper(
                        activity = (getSource() as Fragment).requireActivity(),
                        toastWrapper = get(),
                        fragment = (getSource() as Fragment),
                        ytJavaApi = get(),
                        navController = (getSource() as Fragment).findNavController(),
                        log = get()
                    )
                }
                scoped { YoutubeJavaApiWrapper((getSource() as Fragment).requireActivity() as AppCompatActivity) }
                scoped {
                    ChipCreator((getSource() as Fragment).requireActivity(), get(), get())
                }
                scoped {
                    SelectDialogCreator((getSource() as Fragment).requireActivity())
                }
            }
        }
    }
}
